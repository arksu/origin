# coding=utf-8
bl_info = {
    "name":         "a1 exporter",
    "author":       "arksu",
    "blender":      (2,6,2),
    "version":      (0,0,1),
    "location":     "File > Import-Export",
    "description":  "a1 format",
    "category":     "Import-Export"
}

# наборы анимаций обозначаются маркерами на таймлайне. заканчиватся должны на _start и _end
# пример: action1_start на 15 кадре и action1_end на 27 кадре.
# в анимацию с именем action1 войдут кадры с 15 по 27
# таким образом на одной сцене может присутствовать множество анимаций
# которые можно экспортировать за 1 заход

import bpy
from bpy_extras.io_utils import ExportHelper
from bpy.props import (BoolProperty,
                       FloatProperty,
                       StringProperty,
                       IntProperty
                       )
class a1export_class(bpy.types.Operator, ExportHelper):
    bl_idname       = "export_a1_format.a1blend"
    bl_label        = "a1 Exporter"
    bl_options      = {'PRESET'}

    filename_ext    = ".a1blend"

    do_mesh = BoolProperty(
        name="Export Mesh",
        description="Export mesh data",
        default=True,
    )

    do_skeleton = BoolProperty(
        name="Export Skeleton",
        description="Export all bones in bind pose",
        default=True,
    )

    do_anims = BoolProperty(
        name="Export Animations",
        description="Export all skeleton anims",
        default=True,
    )


    markerFilter = StringProperty(
        name="Marker filter",
        description="Export only frame ranges tagged with "\
                    + "markers whose names start with this (_start, _end)",
        default="",
    )

    scaleFactor = FloatProperty(
        name="Scale",
        description="Scale all data",
        min=0.01, max=1000.0,
        soft_min=0.01,
        soft_max=1000.0,
        default=1.0,
    )

    initFrame = IntProperty(
        name="Bind pose frame",
        description="frame in that armature in bind pose",
        min=0, max=1000,
        soft_min=0,
        soft_max=1000,
        default=0,
    )

    def execute(self, context):
        from . import export_a1
        return export_a1.run(self.filepath, self.markerFilter,
            self.scaleFactor, self.initFrame,
            self.do_mesh, self.do_skeleton, self.do_anims
        )

def menu_func(self, context):
    self.layout.operator(a1export_class.bl_idname, text="a1 mesh (.a1blend)")

def register():
    bpy.utils.register_module(__name__)
    bpy.types.INFO_MT_file_export.append(menu_func)

def unregister():
    bpy.utils.unregister_module(__name__)
    bpy.types.INFO_MT_file_export.remove(menu_func)

if __name__ == "__main__":
    register()