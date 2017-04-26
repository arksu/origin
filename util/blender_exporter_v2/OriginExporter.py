# coding=utf-8
import struct
import bpy
import bmesh
from bpy_extras.io_utils import ExportHelper
import mathutils
import os.path
import math
from mathutils.geometry import normal
from mathutils import Vector

class Error(Exception):
    def __init__(self, message):
        self.message = message


def vertkey(v, n, uv):
    return round(v.x, 4), round(v.y, 4), round(v.z, 4), round(n.x, 4), round(n.y, 4), round(n.z, 4), round(uv[0], 4), round(uv[1], 4),


class BoneClass:
    def __init__(self, name, mw, ml, index, parent_name):
        self.name = name
        self.mw = mw
        self.ml = ml
        self.index = index
        self.parent_name = parent_name

    def getName(self):
        return self.name

    def getMW(self):
        return self.mw

    def getML(self):
        return self.ml

    def getIndex(self):
        return self.index

    def write(self, fw):
        write_string(fw, self.name)

        write_string(fw, self.parent_name)
        # bind
        write_matrix(fw, self.mw)
        # frame
        write_matrix(fw, self.ml)

MAX_WEIGHTS = 2
bind_pose = {}
bones_list = []

def run(filepath, global_matrix, context, scaleFactor, do_mesh, do_skeleton, do_anims,
        do_select_only, do_mesh_modifers, do_binormals, do_all_anims):
    scene = context.scene
    bind_pose.clear()
    bones_list.clear()

    if do_select_only:
        data_seq = context.selected_objects
    else:
        data_seq = scene.objects

    armatureObject = None
    meshObjects = []

    """
    если выбрали скелет то ищем среди объектов скелет.
    а меши будем брать из дочерних объектов скелета
    """

    if do_skeleton:
        all_armatures = [obj for obj in data_seq if obj.type == 'ARMATURE']
        if len(all_armatures) == 1:
            armatureObject = all_armatures[0]
        elif len(all_armatures) > 1:
            raise Error("Please select an armature in the scene")
        else:
            raise Error("No armatures in scene")

        if do_mesh:
            meshObjects = [obj for obj in armatureObject.children if obj.type == 'MESH']
    else:
        if do_mesh:
            meshObjects = [obj for obj in data_seq if obj.type == 'MESH']

    do_weights = armatureObject != None and do_skeleton
    use_ASCII = False
    mode = 'w' if use_ASCII else 'bw'
    with open(filepath, mode) as data:
        fw = data.write

        # SKELETON & ANIMATIONS
        # сначала запишем флаг наличия скелета
        if armatureObject != None and do_skeleton:
            print ("armature obj ", armatureObject)
            fw(struct.pack('>B', 1))
            write_skeleton(fw, armatureObject, use_ASCII)

            if do_anims:
                fw(struct.pack('>B', 1))
                write_all_anims(fw, armatureObject, do_all_anims)
            else:
                fw(struct.pack('>B', 0))
        else:
            fw(struct.pack('>B', 0))

        # MESHES
        # сколько всго мешей
        fw(struct.pack('>I', len(meshObjects)))
        for ob in meshObjects:
            me = ob.to_mesh(scene, do_mesh_modifers, 'PREVIEW', calc_tessface=False)
            me.transform(global_matrix * ob.matrix_world)
            mesh_triangulate(me)
            me.calc_normals()

            # запишем имя меша
            write_string(fw, ob.name)
            write_mesh(fw, me, ob, use_ASCII, do_binormals, do_weights)

            bpy.data.meshes.remove(me)


    return {'FINISHED'}

def write_mesh(fw, me, object, use_ASCII, do_binormals, do_weights):
    face_index_pairs = [(face, index) for index, face in enumerate(me.polygons)]

    vertex_groups = object.vertex_groups
    haveUV = len(me.uv_textures) > 0
    if use_ASCII:
        fw('uv %i\n' % haveUV)

    # нужно отсортировать грани по use_smooth
    if haveUV:
        if do_binormals:
            me.calc_tangents()
        uv_texture = me.uv_textures.active.data[:]
        uv_layer = me.uv_layers.active.data[:]

        sort_func = lambda a: (a[0].material_index,
                               hash(uv_texture[a[1]].image),
                               a[0].use_smooth)
    else:
        sort_func = lambda a: a[0].use_smooth

    face_index_pairs.sort(key=sort_func)
    del sort_func


    vert_list = []
    normal_list = []
    bitangent_list = []
    uv_list = []
    weights_list = []

    index_list = []

    vert_map = {}

    # идем по всем граням
    for face, index in face_index_pairs:
        # по всем вершинам в грани
        indicies = []
        for loop_index in face.loop_indices:
            vi = me.loops[loop_index].vertex_index
            co = me.vertices[vi].co

            # сглажена ли грань? надо брать нормали из вершин. все ок
            if face.use_smooth:
                no = me.vertices[vi].normal
            else:
                no = face.normal

            if do_binormals:
                tangent = me.loops[loop_index].tangent
                bitangent = me.loops[loop_index].bitangent_sign * no.cross(tangent)
                bitangent_sign = me.loops[loop_index].bitangent_sign
                bitangent = Vector((bitangent.x, bitangent.y, bitangent.z, bitangent_sign))

            if haveUV:
                uv = uv_layer[loop_index].uv
                if (uv[0] < 0): uv[0] = 0
                if (uv[0] > 1): uv[0] = 1
                if (uv[1] < 0): uv[1] = 0
                if (uv[1] > 1): uv[1] = 1
            else:
                uv = Vector([0,0])

            if do_weights:
                # weights
                ws = []
                groups = me.vertices[vi].groups
                for vgroup in groups:
                    wg = vertex_groups[vgroup.group]
                    vertex_weight = vgroup.weight
                    bone_name = wg.name
                    bone = get_bone(bone_name)

                    ws.append((vertex_weight, bone.getIndex()))

                lenws = len(ws)
                # если нашли костей которые действуют на эту вершину больше чем допустимо
                if lenws >= MAX_WEIGHTS:
                    # отсортируем по весу. так чтобы в начале шли кости с большим весом
                    ws = sorted(ws, key=lambda w: w[0], reverse=True)
                    # до тех пор пока костей больше чем допустимо - будем удалять с конца списка
                    while len(ws) > MAX_WEIGHTS:
                        ws.pop()
                    totalW = 0
                    for w in ws:
                        totalW += w[0]
                    w1 = ws[0][0] / totalW
                    w2 = 1 - w1
                    ws[0] = (w1, ws[0][1])
                    ws[1] = (w2, ws[1][1])
                else:
                    if lenws == 0:
                        print("LEN 0!")
                    # если костей меньше чем положено
                    if lenws < MAX_WEIGHTS:
                        # добавляем нулевые веса пока не заполним массив
                        while len(ws) < MAX_WEIGHTS:
                            ws.append((0, 0))

            key = vertkey(co, no, uv)

            # ищем такую вершину в списке, получим ее индекс в наших массивах
            new_vi = vert_map.get(key)
            # если такой вершины еще не заносили в список - заведем ее
            if new_vi is None:
                # print("vert NOT found in map ", key)
                vert_list.append(co)
                normal_list.append(no)
                uv_list.append(uv)
                if do_binormals:
                    bitangent_list.append(bitangent)
                if do_weights:
                    weights_list.append(ws)

                new_vi = len(vert_list) - 1
                vert_map[key] = new_vi
                indicies.append(new_vi)
            else:
                # print("found in map ", key, " ", new_vi)
                indicies.append(new_vi)

        index_list.append(indicies)

    if use_ASCII:
        fw('orig vert count %d\n' % len(me.vertices))
        fw('optimized vert count %d\n' % len(vert_list))
        fw('tri count %d\n' % len(me.polygons))
    else:
        # в начале пишем флаги присутствующих данных
        if do_binormals:
            fw(struct.pack('>B', 1))
        else:
            fw(struct.pack('>B', 0))

        if do_weights:
            fw(struct.pack('>B', 1))
        else:
            fw(struct.pack('>B', 0))

        fw(struct.pack('>I', len(vert_list)))


    for i in range(len(vert_list)):
        if use_ASCII:
            fw('vert %f %f %f\n' % vert_list[i][:])
            fw('normal %f %f %f\n' % normal_list[i][:])
            fw('uv %f %f\n' % uv_list[i][:])
        else:
            fw(struct.pack('>fff', vert_list[i][0], vert_list[i][1], vert_list[i][2]))
            fw(struct.pack('>fff', normal_list[i][0], normal_list[i][1], normal_list[i][2]))
            if do_binormals:
                fw(struct.pack('>ffff', bitangent_list[i][0], bitangent_list[i][1], bitangent_list[i][2], bitangent_list[i][3]))
            fw(struct.pack('>ff', uv_list[i][0], 1-uv_list[i][1]))
            if do_weights:
                for ws in weights_list[i]:
                    # weight, index
                    fw(struct.pack('>ff', ws[0], ws[1]))

    if use_ASCII:
        fw('index\n')
    else:
        fw(struct.pack('>I', len(index_list)))

    for i in index_list:
        if use_ASCII:
            fw('   %i %i %i\n' % (i[0], i[1], i[2]))
        else:
            fw(struct.pack('>HHH', i[0], i[1], i[2]))


def write_skeleton(fw, obj, use_ASCII):
    armature = bpy.data.armatures[obj.name]
    arm_mw = obj.matrix_world
    print ("arm ", armature)

    bones = armature.bones
    if not bones:
        raise Error("no bones")

    # ищем кости у которых родитель не в списке костей
    abandonedBones = [i for i in bones
                          if i.parent and i.parent not in bones[:]]
    if abandonedBones:
        boneList = []
        for ab in abandonedBones:
            boneList.append("- " + str(ab.name))
        raise Error("bones missing parents : "+ boneList)

    for b in bones:

        if not b.use_deform:
            print ("not deformable bone!: ", b.name)
            continue

        bone_parent = b.parent
        while bone_parent:
            if bone_parent.use_deform:
                break
            bone_parent = bone_parent.parent

        if bone_parent:
            parent_name = bone_parent.name
        else:
            parent_name = ''

        mw = arm_mw * b.matrix_local  # точно

        # frame
        if bone_parent:
            ml = bone_parent.matrix_local.inverted() * b.matrix_local
        else:
            ml = mw

        idx = len(bones_list)
        bone = BoneClass(b.name, mw.inverted(), ml, idx, parent_name)
        bones_list.append(bone)

        bind_pose[b.name] = ml

    print("bones_list ", len(bones_list))
    fw(struct.pack('>H', len(bones_list)))
    for bone in bones_list:
        print("bone ", bone.getName())
        bone.write(fw)

def write_all_anims(fw, obj, do_all_anims):
    anim_count = len(bpy.data.actions)
    if do_all_anims:
        fw(struct.pack('>H', anim_count))
    else:
        fw(struct.pack('>H', 1))
    default_action = obj.animation_data.action
    if do_all_anims and anim_count > 1:
        for a in bpy.data.actions:
            bpy.context.scene.frame_start = a.frame_range.x
            bpy.context.scene.frame_end = a.frame_range.y
            obj.animation_data.action = a
            write_string(fw, a.name)
            write_anim(fw, obj, a)
        obj.animation_data.action = default_action
    else:
        write_string(fw, default_action.name)
        write_anim(fw, obj, None)

    return 1

def write_anim(fw, obj, action):
    startFrame = bpy.context.scene.frame_start
    endFrame = bpy.context.scene.frame_end

    pBones = obj.pose.bones

    print ("arm :", obj, " pbones: ", pBones, " frames: ", startFrame, " - ", endFrame)

    present_bones = set()
    if (action != None):
        for f in action.fcurves:
            present_bones.add(f.group.name)
        print ("present_bones:")
        print (present_bones)

    # frames count
    fcount = endFrame - startFrame + 1
    fw(struct.pack('>H', fcount))
    fps = bpy.context.scene.render.fps
    fw(struct.pack('>H', fps))

    # frames
    print ("process frames...")
    for frame in range(startFrame, endFrame + 1):
        bpy.context.scene.frame_set(frame)
        print("set frame ", frame)
        for b in bones_list:
            bname = b.getName()
            pBone = pBones[bname]
            bone_parent = pBone.parent
            while bone_parent:
                if bone_parent.bone.use_deform:
                    break
                bone_parent = bone_parent.parent

            pBoneMatrix = pBone.matrix

            if bone_parent:
                diffMatrix = bone_parent.matrix.inverted() * (pBoneMatrix)
            else:
                diffMatrix = obj.matrix_world * pBoneMatrix

            # одинаковые матрицы. запишем флаг для пропуска этой кости по флагам
            if (cmp_matrix(bind_pose[b.name], diffMatrix) or (not (bname in present_bones))) and bone_parent:
                fw(struct.pack('>B', 0))
            else:
                fw(struct.pack('>B', 1))
                fw(struct.pack('>H', b.getIndex()))
                write_matrix(fw, diffMatrix)

def mesh_triangulate(me):
    import bmesh
    bm = bmesh.new()
    bm.from_mesh(me)
    bmesh.ops.triangulate(bm, faces=bm.faces)
    bm.to_mesh(me)
    bm.free()

def get_bone(bone_name):
    for b in bones_list:
        if b.getName() == bone_name:
            return b
    raise Error("bone not found "+bone_name)


def eps_num(n1, n2):
    return (n1 - n2) < 0.00001

def cmp_matrix(m1, m2):
    if \
    eps_num(m1[0][0], m2[0][0]) and eps_num(m1[0][1], m2[0][1]) and eps_num(m1[0][2], m2[0][2]) and eps_num(m1[0][3],m2[0][3]) and \
    eps_num(m1[1][0], m2[1][0]) and eps_num(m1[1][1], m2[1][1]) and eps_num(m1[1][2], m2[1][2]) and eps_num(m1[1][3],m2[1][3]) and \
    eps_num(m1[2][0], m2[2][0]) and eps_num(m1[2][1], m2[2][1]) and eps_num(m1[2][2], m2[2][2]) and eps_num(m1[2][3],m2[2][3]) and \
    eps_num(m1[3][0], m2[3][0]) and eps_num(m1[3][1], m2[3][1]) and eps_num(m1[3][2], m2[3][2]) and eps_num(m1[3][3],m2[3][3]):
        return True
    else:
        return False

def write_string(fw, str):
    l = len(str)
    fw(struct.pack('>H', l))
    fw(bytearray(str, 'ascii'))

def write_matrix(fw, m):
    fw(struct.pack('>ffff', m[0][0], m[1][0], m[2][0], m[3][0]))
    fw(struct.pack('>ffff', m[0][1], m[1][1], m[2][1], m[3][1]))
    fw(struct.pack('>ffff', m[0][2], m[1][2], m[2][2], m[3][2]))
    fw(struct.pack('>ffff', m[0][3], m[1][3], m[2][3], m[3][3]))
