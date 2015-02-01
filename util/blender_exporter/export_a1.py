# coding=utf-8
import struct
import bpy
import bmesh
from bpy_extras.io_utils import ExportHelper
import mathutils
import os.path
import math

#===========================================================================
# Custom exception class
#===========================================================================
class Error( Exception ):

    def __init__(self, message):
        self.message = message

bind_pose = dict()

def run(fpath, markerFilter, scaleFactor, initFrame, do_mesh, do_skeleton, do_anims):
    print ("start a1 export...")
    file = open(fpath, 'bw')

    # set 0 frame for take skeleton pos
    goBack = bpy.context.scene.frame_current
    bpy.context.scene.frame_set(initFrame)

    # try export by udk exporter
    arm, mesh = find_armature_and_mesh()

    if (do_mesh):
        file.write(struct.pack('<b', 1))
        udk_parse_mesh(mesh, file)
    else:
        file.write(struct.pack('<b', 0))

    correctionMatrix = mathutils.Matrix()

    if (len(bpy.data.armatures) > 0 and do_skeleton):
        armature = bpy.data.armatures[arm.name]

        file.write(struct.pack('<b', 1)) # skeleton flag
        write_skeleton(file, armature, correctionMatrix)

        if (do_anims):
            file.write(struct.pack('<b', 1)) # anim flag
            write_all_anims(file, markerFilter, fpath, arm)
        else:
            file.write(struct.pack('<b', 0)) # anim flag
    else:
        file.write(struct.pack('<b', 0)) # skeleton flag
        file.write(struct.pack('<b', 0)) # anim flag


    file.close()

    print ("a1 export done")
    bpy.context.scene.frame_set(goBack)
    return {'FINISHED'}

def udk_parse_mesh(mesh, file):
    option_clamp_uv = True

    #bpy.ops.object.mode_set(mode='OBJECT')
    #error ? on commands for select object?
    print("Mesh object:", mesh.name)
    scene = bpy.context.scene
    for i in scene.objects: i.select = False # deselect all objects
    scene.objects.active	= mesh
    setmesh = mesh
    mesh = triangulate_mesh(mesh)

    #bpy.context.scene.objects.unlink(setmesh)
    print("FACES----:",len(mesh.data.tessfaces))

    discarded_face_count = 0
    vertex_groups = mesh.vertex_groups

    write_string(file, "a1mesh")
    file.write(struct.pack('<I', len(mesh.data.tessfaces)))

    if (mesh.parent):
        matrix = mesh.parent.matrix_world * mesh.matrix_local
    else:
        matrix = mesh.matrix_local

    for face in mesh.data.tessfaces:
        if len(face.vertices) != 3:
            raise Error("Non-triangular face (%i)" % len(face.vertices))

        if not is_1d_face(face, mesh.data):
            face_index	= face.index
            has_uv		= False
            face_uv		= None

            if len(mesh.data.uv_textures) > 0:
                has_uv		= True
                uv_layer	= mesh.data.tessface_uv_textures.active
                face_uv		= uv_layer.data[face_index]
            #size(data) is number of texture faces. Each face has UVs
            #print("DATA face uv: ",len(faceUV.uv), " >> ",(faceUV.uv[0][0]))

            for i in range(3):
                vert_index	= face.vertices[i]
                vert		= mesh.data.vertices[vert_index]
                #assumes 3 UVs Per face (for now)
                if (has_uv):
                    if len(face_uv.uv) != 3:
                        print("WARNING: face has more or less than 3 UV coordinates - writing 0,0...")
                        uv = [0.0, 0.0]
                    else:
                        uv = [face_uv.uv[i][0],face_uv.uv[i][1]] #OR bottom works better # 24 for cube
                else:
                    #print ("No UVs?")
                    uv = [0.0, 0.0]

                #flip V coordinate because UEd requires it and DOESN'T flip it on its own like it
                #does with the mesh Y coordinates. this is otherwise known as MAGIC-2
                uv[1] = 1.0 - uv[1]

                # clamp UV coords if udk_option_clamp_uv is True
                if option_clamp_uv:
                    if (uv[0] > 1):
                        uv[0] = 1
                    if (uv[0] < 0):
                        uv[0] = 0
                    if (uv[1] > 1):
                        uv[1] = 1
                    if (uv[1] < 0):
                        uv[1] = 0

                #matrix = mathutils.Matrix()
                co = matrix * vert.co
#                no = mesh.matrix_local * vert.normal
                no = vert.normal
                no.normalize()


                file.write(struct.pack('<fff', co[0], co[1], co[2]))
                file.write(struct.pack('<fff', no[0], no[1], no[2]))


                #weight_layer = False
                if (len(vert.groups) > 0):
                    file.write(struct.pack('<H', len(vert.groups)))
                    for vgroup in vert.groups:
                        wg = vertex_groups[vgroup.group]
                        vertex_weight	= vgroup.weight
                        wname = wg.name

                        write_string(file, wname)
                        file.write(struct.pack('<f', vertex_weight))
                else:
                    # no weight data
                    file.write(struct.pack('<H', 0))


                file.write(struct.pack('<ff', uv[0], uv[1]))



        #END if not is_1d_face(current_face, mesh.data)
        else:
            discarded_face_count += 1

    print ("discarded_face_count ", discarded_face_count)

    bpy.ops.object.mode_set(mode='OBJECT')	  # OBJECT mode
    mesh.parent = None						  # unparent to avoid phantom links
    bpy.context.scene.objects.unlink(mesh)	  # unlink



# arm - armature object
def write_skeleton(file, armature, correctionMatrix):
#    global orientationTweak
    print("save skeleton...")


    armature_obj = bpy.data.objects[armature.name]
    arm_mw = armature_obj.matrix_world

    bones = armature.bones
    if not bones:
        print("no bones for skeleton")
        return

    abandonedBones = [i for i in bones
                      if i.parent and i.parent not in bones[:]]
    if abandonedBones:
        boneList = []
        for ab in abandonedBones:
            boneList.append("- " + str(ab.name))
        print ("bones missing parents : ", boneList)

    print ("bones count: ", len(bones))
    # header
    write_string(file, "a1skeleton")
    # count
    file.write(struct.pack('<H', len(bones)))
    # data
    for b in bones:
        if not b.use_deform:
            print ("not deformable bone!: ", b.name)
            write_skip(file, True)
            continue
        write_skip(file, False)

        bone_parent = b.parent
        while bone_parent:
            if bone_parent.use_deform:
                break
            bone_parent = bone_parent.parent

        if bone_parent:
            pn = bone_parent.name
        else:
            pn = ''

        mw = arm_mw * b.matrix_local # точно

        if bone_parent:
            ml = bone_parent.matrix_local.inverted() * b.matrix_local
        else:
            ml = mw


#        mw = get_mw(b)

#        ml = correctionMatrix * b.matrix_local

#        print ("m local : ", ml)
#        print ("m world : ", mw)
#        print ("parent", pn)
#        print ("name: ", b.name, "---------")

        write_string(file, b.name)
        write_string(file, pn)
        write_matrix(file, mw.inverted()) # bind
#        inverted = boneMatrix.inverted()
        write_matrix(file, ml) # frame
        bind_pose[b.name] = ml

    print("skeleton saved")

def write_all_anims(file, markerFilter, filePath, arm):
    ranges = get_ranges(markerFilter)
    print ("ranges : ", ranges)
    if ranges:
        file.write(struct.pack('<H', len(ranges)))
        for r in ranges.keys():
#            folder = os.path.dirname(filePath)
#            animFile = os.path.join(folder, r + ".a1anim")
            write_anim(file, r, ranges[r], arm)
    else:
        file.write(struct.pack('<H', 1))
        write_anim(file, None, None, arm)

#        baseFilePathEnd = filePath.rfind(".md5mesh")
#        if baseFilePathEnd == -1:
#            animFilePath = filePath + ".md5anim"
#        else:
#            animFilePath = filePath[:baseFilePathEnd] + ".md5anim"
#        write_md5anim(animFilePath, prerequisites, correctionMatrix, None)
#        return {'FINISHED'}


def write_anim(file, Name, frameRange, armature):
#    global orientationTweak
    print ("save animation...  name: ", Name, " range: ", frameRange)

    write_string(file, "a1anim")
    if frameRange == None:
        startFrame = bpy.context.scene.frame_start
        endFrame = bpy.context.scene.frame_end
    else:
        startFrame, endFrame = frameRange

    #armature = bpy.context.object.find_armature()
    #armature = bpy.data.armatures[0]
    bones = armature.data.bones
    armObj = [o for o in bpy.data.objects if o.data == bones[0].id_data][0]
    pBones = armObj.pose.bones

    print ("arm :", armObj , " pbones: ", pBones)

    # anim name
    if Name:
        write_string(file, Name)
    else:
        write_string(file, '')

    # frames count
    fcount = endFrame - startFrame + 1
    file.write(struct.pack('<H', fcount))
    fps = bpy.context.scene.render.fps
    file.write(struct.pack('<H', fps))

    # bones names
    file.write(struct.pack('<H', len(bones)))
    for b in bones:
        write_string(file, b.name)

#    print ("orientationTweak ", orientationTweak)
    # frames
    print ("process frames...")
    for frame in range(startFrame, endFrame + 1):
        bpy.context.scene.frame_set(frame)
        #print("set frame ", frame)
        for b in bones:
            if not b.use_deform:
                write_skip(file, True)
                continue

            write_skip(file, False)

            pBone = pBones[b.name]
            bone_parent = pBone.parent
            while bone_parent:
                if bone_parent.bone.use_deform:
                    break
                bone_parent = bone_parent.parent



            pBoneMatrix = pBone.matrix

            if bone_parent:
                diffMatrix = bone_parent.matrix.inverted() * (pBoneMatrix)
            else:
                diffMatrix = armObj.matrix_world * pBoneMatrix
#                diffMatrix = orientationTweak * diffMatrix

            # print ("bind_pose ", b.name, "=", bind_pose[b.name])
            # print ("frame matrix=", diffMatrix)

            # одинаковые матрицы. запишем флаг для пропуска этой кости по флагам
            # if cmp_matrix(bind_pose[b.name], diffMatrix):
            #     print("equal matrix ", b.name)
            #     write_skip(file, True)
            # else:
            #     write_skip(file, False)

            write_matrix(file, diffMatrix)

    print ("animation saved")
    pass

def get_mw(bone):
    ml = bone.matrix_local
    if (bone.parent):
        ml = get_mw(bone.parent) * ml
#    else:
#        ml = bpy.data.objects['Armature'].matrix_world * ml
    return ml

def triangulate(bm):
    while True:
        nonTris = [f for f in bm.faces if len(f.verts) > 3]
        if nonTris:
            nt = nonTris[0]
            pivotLoop = nt.loops[0]
            allVerts = nt.verts
            vert1 = pivotLoop.vert
            wrongVerts = [vert1,
                          pivotLoop.link_loop_next.vert,
                          pivotLoop.link_loop_prev.vert]
            bmesh.utils.face_split(nt, vert1, [v for v in allVerts
                                               if v not in wrongVerts][0])
            for seq in [bm.verts, bm.faces, bm.edges]: seq.index_update()
        else:
            break
    return bm

def write_string(file, str):
    l = len(str)
    file.write(struct.pack('<H', l))
    file.write(bytearray(str, 'ascii'))

def write_matrix(file, m):
    # transpose in converter
    file.write(struct.pack('<ffff', m[0][0], m[0][1], m[0][2], m[0][3]))
    file.write(struct.pack('<ffff', m[1][0], m[1][1], m[1][2], m[1][3]))
    file.write(struct.pack('<ffff', m[2][0], m[2][1], m[2][2], m[2][3]))
    file.write(struct.pack('<ffff', m[3][0], m[3][1], m[3][2], m[3][3]))

def eps_num(n1, n2):
    return (n1-n2) < 0.00001

def cmp_matrix(m1, m2):
    if \
    eps_num(m1[0][0], m2[0][0]) and eps_num(m1[0][1], m2[0][1]) and eps_num(m1[0][2], m2[0][2]) and eps_num(m1[0][3], m2[0][3]) and \
    eps_num(m1[1][0], m2[1][0]) and eps_num(m1[1][1], m2[1][1]) and eps_num(m1[1][2], m2[1][2]) and eps_num(m1[1][3], m2[1][3]) and \
    eps_num(m1[2][0], m2[2][0]) and eps_num(m1[2][1], m2[2][1]) and eps_num(m1[2][2], m2[2][2]) and eps_num(m1[2][3], m2[2][3]) and \
    eps_num(m1[3][0], m2[3][0]) and eps_num(m1[3][1], m2[3][1]) and eps_num(m1[3][2], m2[3][2]) and eps_num(m1[3][3], m2[3][3]):
        return True
    else:
        return False

def write_skip(file, skip):
    if skip:
        file.write(struct.pack('<b', 1))
    else:
        file.write(struct.pack('<b', 0))

def get_ranges(markerFilter):
    markers = bpy.context.scene.timeline_markers
    starts = [m for m in markers if
              m.name.startswith(markerFilter)
              and m.name.endswith("_start", 2)]
    ends = [m for m in markers if
            m.name.startswith(markerFilter)
            and m.name.endswith("_end", 2)]
    if not starts or not ends:
        return None
    else:
        return find_matches(starts, ends)

def find_matches(starts, ends):
    pairs = {}
    for s in starts:
        basename = s.name[:s.name.rfind("_start")]
        matches = [e for e in ends if
                   e.name[:e.name.rfind("_end")] == basename]
        if matches:
            m = matches[0]
            pairs[basename] = (min(s.frame, m.frame), max(s.frame, m.frame))
    return pairs


#===========================================================================
# Locate the target armature and mesh for export
# RETURNS armature, mesh
#===========================================================================
def find_armature_and_mesh():
    print ("find_armature_and_mesh")
    context			= bpy.context
    active_object	= context.active_object
    armature		= None
    mesh			= None

    # TODO:
    # this could be more intuitive
    bpy.ops.object.mode_set(mode='OBJECT')
    # try the active object
    if active_object and active_object.type == 'ARMATURE':
        armature = active_object

    # otherwise, try for a single armature in the scene
    else:
        all_armatures = [obj for obj in context.scene.objects if obj.type == 'ARMATURE']

        if len(all_armatures) == 1:
            armature = all_armatures[0]

        elif len(all_armatures) > 1:
            raise Error("Please select an armature in the scene")

        else:
            raise Error("No armatures in scene")

    print ("Found armature: ", armature.name, " ", armature)

    meshselected = []
    parented_meshes = [obj for obj in armature.children if obj.type == 'MESH']
    for obj in armature.children:
        #print(dir(obj))
        if obj.type == 'MESH' and obj.select == True:
            meshselected.append(obj)
    # try the active object
    if active_object and active_object.type == 'MESH' and len(meshselected) == 0:

        if active_object.parent == armature:
            mesh = active_object

        else:
            raise Error("The selected mesh is not parented to the armature")

    # otherwise, expect a single mesh parented to the armature (other object types are ignored)
    else:
        print("Number of meshes:",len(parented_meshes))
        print("Number of meshes (selected):",len(meshselected))
        if len(parented_meshes) == 1:
            mesh = parented_meshes[0]

        elif len(parented_meshes) > 1:
            if len(meshselected) >= 1:
                mesh = sortmesh(meshselected)
            else:
                raise Error("More than one mesh(s) parented to armature. Select object(s)!")
        else:
            raise Error("No mesh parented to armature")

    print ("Found mesh: " +mesh.name, " ", mesh)
#    if len(armature.pose.bones) == len(mesh.vertex_groups):
#        print("Armature and Mesh Vertex Groups matches Ok!")
#    else:
#        raise Error("Armature bones:" + str(len(armature.pose.bones)) + " Mesh Vertex Groups:" + str(len(mesh.vertex_groups)) +" doesn't match!")
    return armature, mesh

#copy mesh data and then merge them into one object
def meshmerge(selectedobjects):
    bpy.ops.object.mode_set(mode='OBJECT')
    cloneobjects = []
    if len(selectedobjects) > 1:
        print("selectedobjects:",len(selectedobjects))
        count = 0 #reset count
        for count in range(len( selectedobjects)):
            #print("Index:",count)
            if selectedobjects[count] != None:
                me_da = selectedobjects[count].data.copy() #copy data
                me_ob = selectedobjects[count].copy() #copy object
                #note two copy two types else it will use the current data or mesh
                me_ob.data = me_da
                bpy.context.scene.objects.link(me_ob)#link the object to the scene #current object location
                print("Index:",count,"clone object",me_ob.name)
                cloneobjects.append(me_ob)
            #bpy.ops.object.mode_set(mode='OBJECT')
        for i in bpy.data.objects: i.select = False #deselect all objects
        count = 0 #reset count
        #bpy.ops.object.mode_set(mode='OBJECT')
        for count in range(len( cloneobjects)):
            if count == 0:
                bpy.context.scene.objects.active = cloneobjects[count]
                print("Set Active Object:",cloneobjects[count].name)
            cloneobjects[count].select = True
        bpy.ops.object.join()
        if len(cloneobjects) > 1:
            bpy.types.Scene.udk_copy_merge = True
    return cloneobjects[0]

#sort the mesh center top list and not center at the last array. Base on order while select to merge mesh to make them center.
def sortmesh(selectmesh):
    print("MESH SORTING...")
    centermesh = []
    notcentermesh = []
    for countm in range(len(selectmesh)):
        if selectmesh[countm].location.x == 0 and selectmesh[countm].location.y == 0 and selectmesh[countm].location.z == 0:
            centermesh.append(selectmesh[countm])
        else:
            notcentermesh.append(selectmesh[countm])
    selectmesh = []
    for countm in range(len(centermesh)):
        selectmesh.append(centermesh[countm])
    for countm in range(len(notcentermesh)):
        selectmesh.append(notcentermesh[countm])
    if len(selectmesh) == 1:
        return selectmesh[0]
    else:
        return meshmerge(selectmesh)


#===========================================================================
# http://en.wikibooks.org/wiki/Blender_3D:_Blending_Into_Python/Cookbook#Triangulate_NMesh
# blender 2.50 format using the Operators/command convert the mesh to tri mesh
#===========================================================================
def triangulate_mesh( object ):

    print("triangulateNMesh")
    #print(type(object))
    scene = bpy.context.scene

    me_ob		= object.copy()
    me_ob.data = object.to_mesh(bpy.context.scene, True, 'PREVIEW') #write data object
    bpy.context.scene.objects.link(me_ob)
    bpy.context.scene.update()
    bpy.ops.object.mode_set(mode='OBJECT')
    for i in scene.objects:
        i.select = False # deselect all objects

    me_ob.select			= True
    scene.objects.active	= me_ob

    print("Copy and Convert mesh just incase any way...")

    bpy.ops.object.mode_set(mode='EDIT')
    bpy.ops.mesh.select_all(action='SELECT')# select all the face/vertex/edge
    bpy.ops.object.mode_set(mode='EDIT')
    bpy.ops.mesh.quads_convert_to_tris()
    bpy.context.scene.update()

    bpy.ops.object.mode_set(mode='OBJECT')

    print("Triangulated mesh")

    me_ob.data = me_ob.to_mesh(bpy.context.scene, True, 'PREVIEW') #write data object
    bpy.context.scene.update()
    return me_ob


#===========================================================================
#RG - check to make sure face isnt a line
#===========================================================================
def is_1d_face( face, mesh ):
    #ID Vertex of id point
    v0 = face.vertices[0]
    v1 = face.vertices[1]
    v2 = face.vertices[2]

    return (mesh.vertices[v0].co == mesh.vertices[v1].co\
            or mesh.vertices[v1].co == mesh.vertices[v2].co\
            or mesh.vertices[v2].co == mesh.vertices[v0].co)
    return False