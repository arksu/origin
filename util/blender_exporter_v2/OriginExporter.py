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

def run(filepath, global_matrix, context, scaleFactor, do_mesh, do_skeleton, do_anims,
        do_select_only, do_mesh_modifers):
    scene = context.scene

    if do_select_only:
        data_seq = context.selected_objects
    else:
        data_seq = scene.objects

    armature = None
    meshObjects = []

    """
    если выбрали скелет то ищем среди объектов скелет.
    а меши будем брать из дочерних объектов скелета
    """

    if do_skeleton:
        all_armatures = [obj for obj in data_seq if obj.type == 'ARMATURE']
        if len(all_armatures) == 1:
            armature = all_armatures[0]
        elif len(all_armatures) > 1:
            raise Error("Please select an armature in the scene")
        else:
            raise Error("No armatures in scene")

        if do_mesh:
            meshObjects = [obj for obj in armature.children if obj.type == 'MESH']
    else:
        if do_mesh:
            meshObjects = [obj for obj in data_seq if obj.type == 'MESH']


    use_ASCII = False
    mode = 'w' if use_ASCII else 'bw'
    with open(filepath, mode) as data:
        fw = data.write

        fw(struct.pack('>I', len(meshObjects)))
        for ob in meshObjects:
            me = ob.to_mesh(scene, do_mesh_modifers, 'PREVIEW', calc_tessface=False)
            me.transform(global_matrix * ob.matrix_world)
            mesh_triangulate(me)
            me.calc_normals()

            write_mesh(fw, me, use_ASCII)

            bpy.data.meshes.remove(me)

        if armature != None:
            for a in armature:
                print ("armature ", a)

    return {'FINISHED'}

def write_mesh(fw, me, use_ASCII):
    face_index_pairs = [(face, index) for index, face in enumerate(me.polygons)]


    haveUV = len(me.uv_textures) > 0
    if use_ASCII:
        fw('uv %i\n' % haveUV)

    # нужно отсортировать грани по use_smooth
    if haveUV:
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
    uv_list = []

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
                # print("is flat")
                no = face.normal

            if haveUV:
                uv = uv_layer[loop_index].uv
                if (uv[0] < 0): uv[0] = 0
                if (uv[0] > 1): uv[0] = 1
                if (uv[1] < 0): uv[1] = 0
                if (uv[1] > 1): uv[1] = 1
            else:
                uv = Vector([0,0])

            key = vertkey(co, no, uv)

            # ищем такую вершину в списке, получим ее индекс в наших массивах
            new_vi = vert_map.get(key)
            # если такой вершины еще не заносили в список - заведем ее
            if new_vi is None:
                # print("vert NOT found in map ", key)
                vert_list.append(co)
                normal_list.append(no)
                uv_list.append(uv)

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
        fw(struct.pack('>I', len(vert_list)))


    for i in range(len(vert_list)):
        if use_ASCII:
            fw('vert %f %f %f\n' % vert_list[i][:])
            fw('normal %f %f %f\n' % normal_list[i][:])
            fw('uv %f %f\n' % uv_list[i][:])
        else:
            fw(struct.pack('>fff', vert_list[i][0], vert_list[i][1], vert_list[i][2]))
            fw(struct.pack('>fff', normal_list[i][0], normal_list[i][1], normal_list[i][2]))
            fw(struct.pack('>ff', uv_list[i][0], 1-uv_list[i][1]))

    if use_ASCII:
        fw('index\n')
    else:
        fw(struct.pack('>I', len(index_list)))

    for i in index_list:
        if use_ASCII:
            fw('   %i %i %i\n' % (i[0], i[1], i[2]))
        else:
            fw(struct.pack('>HHH', i[0], i[1], i[2]))


def mesh_triangulate(me):
    import bmesh
    bm = bmesh.new()
    bm.from_mesh(me)
    bmesh.ops.triangulate(bm, faces=bm.faces)
    bm.to_mesh(me)
    bm.free()