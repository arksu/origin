bl_info = {
    "name": "Origin format (.mdl)",
    "author": "arksu",
    "blender": (2, 78, 0),
    "version": (0, 0, 2),
    "location": "File > Import-Export",
    "description": "Export Origin model format",
    "category": "Import-Export"
}

import bpy

from bpy_extras.io_utils import (
    ExportHelper,
    orientation_helper_factory,
    axis_conversion,
)

from bpy.props import (BoolProperty,
                       FloatProperty,
                       StringProperty,
                       IntProperty
                       )


IOMDLOrientationHelper = orientation_helper_factory("IOMDLOrientationHelper", axis_forward='Z', axis_up='Y')


class OriginExportClass(bpy.types.Operator, ExportHelper, IOMDLOrientationHelper):
    bl_idname = "export_mesh_origin.mdl"
    bl_label = "Origin export"
    bl_options = {'PRESET'}

    filename_ext = ".mdl"

    do_select_only = BoolProperty(
        name="Selection Only",
        description="Export selected objects only",
        default=False,
    )

    do_mesh = BoolProperty(
        name="Export Mesh",
        description="Export mesh data",
        default=True,
    )

    do_skeleton = BoolProperty(
        name="Export Skeleton",
        description="Export skeleton (armature)",
        default=False,
    )

    do_anims = BoolProperty(
        name="Export Animations",
        description="Export all anims (anim actions)",
        default=False,
    )

    scaleFactor = FloatProperty(
        name="Scale",
        description="Scale all data",
        min=0.01, max=1000.0,
        soft_min=0.01,
        soft_max=1000.0,
        default=1.0,
    )


    def execute(self, context):
        # return {'FINISHED'}
        from . import OriginExporter
        from mathutils import Matrix

        scene = context.scene
        # Take into account scene's unit scale, so that 1 inch in Blender gives 1 inch elsewhere! See T42000.
        global_scale = self.scaleFactor
        use_scene_unit = False
        if context.scene.unit_settings.system != 'NONE' and use_scene_unit:
            global_scale *= scene.unit_settings.scale_length

        global_matrix = axis_conversion(to_forward=self.axis_forward,
                                        to_up=self.axis_up,
                                        ).to_4x4() * Matrix.Scale(global_scale, 4)

        return OriginExporter.run(self.filepath, global_matrix, context, self.scaleFactor, self.do_mesh, self.do_skeleton, self.do_anims, self.do_select_only)


def menu_func(self, context):
    self.layout.operator(OriginExportClass.bl_idname, text="Origin model (.mdl)")


def register():
    bpy.utils.register_module(__name__)
    bpy.types.INFO_MT_file_export.append(menu_func)


def unregister():
    bpy.utils.unregister_module(__name__)
    bpy.types.INFO_MT_file_export.remove(menu_func)


if __name__ == "__main__":
    register()