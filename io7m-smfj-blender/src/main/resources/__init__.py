#
# Copyright © 2016 <code@io7m.com> http://io7m.com
#
# Permission to use, copy, modify, and/or distribute this software for any
# purpose with or without fee is hereby granted, provided that the above
# copyright notice and this permission notice appear in all copies.
#
# THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
# WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
# MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
# ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
# WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
# ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
# OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
#

bl_info = {
  "name":        "SMF format",
  "author":      "io7m",
  "version":     (0, 2, 0),
  "blender":     (2, 66, 0),
  "location":    "File > Export > SMF (.smft)",
  "description": "Export meshes to SMF format",
  "warning":     "",
  "wiki_url":    "",
  "tracker_url": "https://github.com/io7m/smf/issues",
  "category":    "Import-Export"
}

import bpy

class ExportSMF(bpy.types.Operator):
  bl_idname = "export_scene.smft"
  bl_label = "Export SMF"

  # This property ends up being assigned by context.window_manager.fileselect_add()
  directory = bpy.props.StringProperty(subtype='DIR_PATH')
  verbose = bpy.props.BoolProperty(name="Verbose logging",description="Enable verbose debug logging",default=True)

  __select_items = []
  __select_items.append(('EXPORT_ALL','All','Export all mesh objects'))
  __select_items.append(('EXPORT_SELECTED','Selected','Export all selected mesh objects'))
  selection = bpy.props.EnumProperty(items=__select_items,name="Export",description="Specify which items should be exported")

  def execute(self, context):
    args = {}
    args['verbose'] = self.verbose
    assert type(args['verbose']) == bool
    args['export_selection'] = self.selection

    from . import export
    e = export.SMFExporter(args)

    try:
      e.write(self.directory)
    except export.SMFExportFailed as ex:
      self.report({'ERROR'}, ex.value)
    #endtry

    return {'FINISHED'}
  #end

  def invoke(self, context, event):
    context.window_manager.fileselect_add(self)
    return {'RUNNING_MODAL'}
  #end
#endclass

def menuFunction(self, context):
  self.layout.operator(ExportSMF.bl_idname, text="SMF (.smft)")
#end

def register():
  bpy.utils.register_class(ExportSMF)
  bpy.types.INFO_MT_file_export.append(menuFunction)
#end

def unregister():
  bpy.utils.unregister_class(ExportSMF)
  bpy.types.INFO_MT_file_export.remove(menuFunction)
#end

if __name__ == "__main__":
  register()
#endif
