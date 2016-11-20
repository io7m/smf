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

import bpy
import bpy_extras.io_utils
import bpy_types
import datetime
import io
import mathutils

class SMFNoMeshSelected(Exception):
  def __init__(self, value):
    self.value = value
  #end
  def __str__(self):
    return repr(self.value)
  #end
#endclass

class SMFExportFailed(Exception):
  def __init__(self, value):
    self.value = value
  #end
  def __str__(self):
    return repr(self.value)
  #end
#endclass

class SMFVertex:
  position = mathutils.Vector((0.0, 0.0, 0.0))
  normal   = mathutils.Vector((0.0, 0.0, 0.0))

  def __init__(self):
    self.position = mathutils.Vector((0.0, 0.0, 0.0))
    self.normal   = mathutils.Vector((0.0, 0.0, 0.0))
  #end
#endclass

SMF_LOG_MESSAGE_DEBUG = 0
SMF_LOG_MESSAGE_INFO  = 1
SMF_LOG_MESSAGE_ERROR = 2

class SMFLogger:
  severity = SMF_LOG_MESSAGE_DEBUG
  counts   = {}
  __file   = None

  def __init__(self, severity, file):
    assert type(severity) == int
    assert type(file) == io.TextIOWrapper

    self.__file   = file
    self.severity = severity
    self.counts   = {}
    self.counts[SMF_LOG_MESSAGE_DEBUG] = 0
    self.counts[SMF_LOG_MESSAGE_INFO]  = 0
    self.counts[SMF_LOG_MESSAGE_ERROR] = 0

    self.debug("debug logging enabled")
  #end

  def __name(self, severity):
    assert type(severity) == int
    if severity == SMF_LOG_MESSAGE_DEBUG:
      return "debug"
    #end
    if severity == SMF_LOG_MESSAGE_INFO:
      return "info"
    #end
    if severity == SMF_LOG_MESSAGE_ERROR:
      return "error"
    #end
  #end

  def log(self, severity, message):
    assert type(severity) == int
    assert type(message) == str

    self.counts[severity] = self.counts[severity] + 1
    if severity >= self.severity:
      text = "smf: " + self.__name(severity) + ": " + message
      self.__file.write(text + "\n")
      print(text)
    #endif
  #end

  def error(self, message):
    self.log(SMF_LOG_MESSAGE_ERROR, message)
  #end

  def debug(self, message):
    self.log(SMF_LOG_MESSAGE_DEBUG, message)
  #end

  def info(self, message):
    self.log(SMF_LOG_MESSAGE_INFO, message)
  #end

#endclass

class SMFTriangle:
  __v0 = 0
  __v1 = 0
  __v2 = 0

  def __init__(self, v0, v1, v2):
    assert type(v0) == int
    assert type(v1) == int
    assert type(v2) == int
    self.__v0 = v0
    self.__v1 = v1
    self.__v2 = v2
  #end
#endclass

class SMFMesh:
  __vertices  = []
  __triangles = []
  __logger    = None

  def __init__(self, logger, vertex_count):
    assert type(logger) == SMFLogger
    assert type(vertex_count) == int

    self.__logger = logger
    for index in range(vertex_count):
      self.__vertices.append(SMFVertex())
    #endfor

    self.__logger.debug("Created mesh with %d vertices" % vertex_count)
  #end

  def addVertex(self, index, vertex):
    assert type(index) == int
    assert type(vertex) == SMFVertex

    return 0
  #end

  def addTriangle(self, triangle):
    assert type(triangle) == SMFTriangle
  #end

#endclass

class SMFExporter:
  __axis_matrix     = bpy_extras.io_utils.axis_conversion(to_forward='-Z', to_up='Y').to_4x4()
  __logger          = None
  __logger_severity = SMF_LOG_MESSAGE_DEBUG

  def __init__(self, options):
    assert type(options) == type({})

    v = options['verbose']
    assert type(v) == bool

    if v:
      self.__logger_severity = SMF_LOG_MESSAGE_DEBUG
    else:
      self.__logger_severity = SMF_LOG_MESSAGE_INFO
    #end
  #end

  def __transformScaleToExport(self, v):
    assert type(v) == mathutils.Vector
    return mathutils.Vector((v.x, v.z, v.y))
  #end

  def __transformTranslationToExport(self, v):
    assert type(v) == mathutils.Vector
    return self.__axis_matrix * v
  #end

  def __transformOrientationToExport(self, q):
    assert type(q) == mathutils.Quaternion

    aa = q.to_axis_angle()
    axis = aa[0]
    axis = self.__axis_matrix * axis
    return mathutils.Quaternion(axis, aa[1])
  #end

  #
  # Meshes should be exported without any armature modifiers being
  # applied. This function makes a copy of the given mesh with all
  # modifiers (that aren't armature modifiers) applied.
  #

  def __buildMeshCopyWithModifiersApplied(self, mesh):
    assert type(mesh) == bpy_types.Object
    assert mesh.type == 'MESH'

    deactivated = []
    for modifier in mesh.modifiers:
      if modifier.type == 'ARMATURE' and modifier.show_viewport:
        self.__logger.debug("__buildMesh: deactivating preview for %s" % mesh.name)
        deactivated.append(modifier)
        modifier.show_viewport = false
      #endif
    #endfor

    mesh_copy = mesh.to_mesh(bpy.context.scene, True, 'PREVIEW')

    for modifier in deactivated:
      self.__logger.debug("__buildMesh: reactivating preview for %s" % mesh.name)
      deactivated.append(modifier)
      modifier.show_viewport = true
    #endfor

    return mesh_copy
  #end

  #
  # Meshes must be triangulated for export.
  #

  def __buildMeshTriangulate(self, mesh):
    assert type(mesh) == bpy_types.Mesh

    self.__logger.debug("__buildMeshTriangulate: triangulating %s" % mesh.name)

    import bmesh
    bm = bmesh.new()
    bm.from_mesh(mesh)
    bmesh.ops.triangulate(bm, faces=bm.faces)
    bm.to_mesh(mesh)
    bm.free()
  #end

  #
  # Build an SMF mesh from a triangulated mesh.
  #

  def __buildSMFMeshFromTriangulated(self, mesh):
    assert type(mesh) == bpy_types.Mesh

    smf_mesh = SMFMesh(self.__logger, len(mesh.vertices))

    for poly in mesh.polygons:
      assert len (poly.vertices) == 3

      lo0 = poly.loop_start + 0
      lo1 = poly.loop_start + 1
      lo2 = poly.loop_start + 2

      vi0 = mesh.loops[lo0].vertex_index
      vi1 = mesh.loops[lo1].vertex_index
      vi2 = mesh.loops[lo2].vertex_index

      v0 = SMFVertex()
      v0.position = self.__transformTranslationToExport(mesh.vertices[vi0].co)
      v0.normal   = self.__transformTranslationToExport(mesh.vertices[vi0].normal)

      v1 = SMFVertex()
      v1.position = self.__transformTranslationToExport(mesh.vertices[vi1].co)
      v1.normal   = self.__transformTranslationToExport(mesh.vertices[vi1].normal)

      v2 = SMFVertex()
      v2.position = self.__transformTranslationToExport(mesh.vertices[vi2].co)
      v2.normal   = self.__transformTranslationToExport(mesh.vertices[vi2].normal)

      new_vi0 = smf_mesh.addVertex(vi0, v0)
      new_vi1 = smf_mesh.addVertex(vi1, v1)
      new_vi2 = smf_mesh.addVertex(vi2, v2)

      smf_mesh.addTriangle(SMFTriangle(new_vi0, new_vi1, new_vi2))
    #endfor

    return smf_mesh
  #end

  def __buildMesh(self, mesh):
    assert type(mesh) == bpy_types.Object
    assert mesh.type == 'MESH'

    mesh_copy = self.__buildMeshCopyWithModifiersApplied(mesh)
    self.__buildMeshTriangulate(mesh_copy)
    self.__logger.debug("__buildMesh: %s" % type(mesh_copy))

    smf = self.__buildSMFMeshFromTriangulated(mesh_copy)
    self.__logger.debug("__buildMesh: freeing %s" % mesh_copy.name)
    bpy.data.meshes.remove(mesh_copy)

    return smf
  #end

  def __writeMesh(self, out_file, smf_mesh):
    assert type(out_file) == io.TextIOWrapper
    assert type(smf_mesh) == SMFMesh

    out_file.write("attribute \"POSITION\" 3 32\n")
    out_file.write("attribute \"NORMAL\"   3 32\n")
  #end

  def __writeFile(self, out_file, mesh):
    assert type(out_file) == io.TextIOWrapper
    assert type(mesh) == bpy_types.Object
    assert mesh.type == 'MESH'

    out_file.write("smf 1 0\n")

    out = self.__buildMesh(mesh)
    self.__logger.debug("__writeFile: type %s" % type(mesh))
    self.__writeMesh(out_file, out)
  #end

  def write(self, path):
    assert type(path) == str
    error_path = path + ".log"

    with open(error_path, "wt") as error_file:
      self.__logger = SMFLogger(self.__logger_severity, error_file)
      t = datetime.datetime.now()
      self.__logger.info("Export started at %s" % t.isoformat())
      self.__logger.info("File: %s" % path)
      self.__logger.info("Log:  %s" % error_path)

      mesh = False
      if len(bpy.context.selected_objects) > 0:
        for obj in bpy.context.selected_objects:
          if obj.type == 'MESH':
            if mesh:
              message = "Too many meshes selected: At most one of the selected objects can be a mesh when exporting"
              self.__logger.error(message)
              raise SMFTooManyMeshesSelected(message)
            #endif
            mesh = obj
          #endif
        #endfor
      #endif

      if False == mesh:
        message = "No meshes selected: A mesh object must be selected for export"
        self.__logger.error(message)
        raise SMFNoMeshSelected(message)
      #endif

      assert type(mesh) == bpy_types.Object
      assert mesh.type == 'MESH'

      self.__logger.debug("opening: %s" % path)
      with open(path, "wt") as out_file:
        self.__writeFile(out_file, mesh)

        errors = self.__logger.counts[SMF_LOG_MESSAGE_ERROR]
        if errors > 0:
          self.__logger.error("Export failed with %d errors." % errors)
          raise SMFExportFailed("Exporting failed due to errors.\nSee the log file at: %s" % error_path)
        else:
          self.__logger.info("Exported successfully")
        #endif
      #endwith
    #endwith
  #end

#endclass
