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

import base64
import bmesh
import bpy
import bpy_extras.io_utils
import bpy_types
import datetime
import io
import mathutils
import os

class SMFExportFailed(Exception):
  def __init__(self, value):
    self.value = value
  #end
  def __str__(self):
    return repr(self.value)
  #end
#endclass

class SMFVertex:
  def __init__(self):
    self.position = mathutils.Vector((0.0, 0.0, 0.0))
    self.normal   = mathutils.Vector((0.0, 0.0, 0.0))
    self.uv       = {}
    self.groups   = {}
  #end
#endclass

SMF_LOG_MESSAGE_DEBUG = 0
SMF_LOG_MESSAGE_INFO  = 1
SMF_LOG_MESSAGE_ERROR = 2

#
# A logger that writes to a log file and stdout.
#

class SMFLogger:
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

#
# A tuple of vertex indices
#

class SMFTriangle:
  def __init__(self, v0, v1, v2):
    assert type(v0) == int
    assert type(v1) == int
    assert type(v2) == int
    self.vertex0 = v0
    self.vertex1 = v1
    self.vertex2 = v2
  #end
#endclass

#
# The data extracted from a mesh object. To produce an SMFMesh, the following
# items are necessary:
#
# 1. A copy of the original mesh data with all non-armature modifiers applied
# 2. A BMesh constructed from the mesh data copy
# 3. The set of vertex groups from the original object
#

class SMFTriangulatedInputMesh:
  def __init__(self, logger, vertex_groups, mesh_data, b_mesh):
    assert type(logger) == SMFLogger
    assert type(vertex_groups) == bpy.types.bpy_prop_collection
    assert type(mesh_data) == bpy.types.Mesh
    assert type(b_mesh) == bmesh.types.BMesh

    self.__logger      = logger
    self.vertex_groups = vertex_groups
    self.b_mesh        = b_mesh
    self.mesh_data     = mesh_data
  #end

  def free(self):
    self.__logger.debug("SMFTriangulatedInputMesh: freeing %s" % self.mesh_data)
    bpy.data.meshes.remove(self.mesh_data)
    self.__logger.debug("SMFTriangulatedInputMesh: freeing %s" % self.b_mesh)
    self.b_mesh.free()
  #end
#endclass

class SMFMesh:

  def __init__(self, logger, vertex_count):
    assert type(logger) == SMFLogger
    assert type(vertex_count) == int

    self.vertices = []
    self.triangles = []
    self.triangles_index_size = 32
    self.uv_attributes = None
    self.groups = None
    self.__logger = logger

    for index in range(vertex_count):
      self.vertices.append(None)
    #endfor

    self.__logger.debug("SMFMesh: created mesh with %d vertices" % vertex_count)
  #end

  def addVertex(self, index, vertex):
    assert type(index) == int
    assert type(vertex) == SMFVertex

    self.__logger.debug("SMFMesh: addVertex: %.15f %.15f %.15f %s" % (vertex.position.x, vertex.position.y, vertex.position.z, vertex))
    self.__logger.debug("SMFMesh: addVertex: uv %s %s" % (self.uv_attributes, sorted(vertex.uv.keys())))
    self.__logger.debug("SMFMesh: addVertex: groups %s %s" % (self.groups, sorted(vertex.groups.keys())))

    # Ensure that all incoming vertices have the same UV configuration
    if self.uv_attributes != None:
      assert self.uv_attributes == sorted(vertex.uv.keys())
    else:
      self.uv_attributes = sorted(vertex.uv.keys())
    #endif

    # Ensure that all incoming vertices have the same groups
    if self.groups != None:
      assert self.groups == sorted(vertex.groups.keys())
    else:
      self.groups = sorted(vertex.groups.keys())
    #endif

    existing = self.vertices[index]
    if existing == None:
      self.__logger.debug("SMFMesh: addVertex: [%d] assigned %s" % (index, vertex))
      self.vertices[index] = vertex
      return index
    #endif

    assert type(existing) == SMFVertex
    assert existing.uv.keys() == vertex.uv.keys()
    assert existing.groups.keys() == vertex.groups.keys()

    if existing.uv == vertex.uv and existing.groups == vertex.groups:
      self.__logger.debug("SMFMesh: addVertex: [%d] vertices are compatible, reusing" % index)
      return index
    #endif

    new_index = len(self.vertices)
    self.__logger.debug("SMFMesh: addVertex: [%d] vertices are incompatible, assigning to %d" % (index, new_index))
    self.vertices.append(vertex)
    return new_index
  #end

  def addTriangle(self, triangle):
    assert type(triangle) == SMFTriangle
    self.__logger.debug("SMFMesh: addTriangle: %d %d %d %s" % (triangle.vertex0, triangle.vertex1, triangle.vertex2, triangle))
    self.triangles.append(triangle)
  #end

#endclass

class SMFExporter:

  def __init__(self, options):
    assert type(options) == type({})

    self.__axis_matrix     = bpy_extras.io_utils.axis_conversion(to_forward='-Z', to_up='Y').to_4x4()
    self.__logger          = None
    self.__logger_severity = SMF_LOG_MESSAGE_DEBUG

    v = options['verbose']
    assert type(v) == bool

    self.__selection = options['export_selection']
    assert type(self.__selection) == str

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

  def __buildMeshCopyWithModifiersAppliedAndTriangulated(self, mesh):
    assert type(mesh) == bpy_types.Object
    assert mesh.type == 'MESH'

    self.__logger.debug("__buildMeshCopyWithModifiersAppliedAndTriangulated: %s" % mesh.name)

    deactivated = []
    for modifier in mesh.modifiers:
      if modifier.type == 'ARMATURE' and modifier.show_viewport:
        self.__logger.debug("__buildMesh: deactivating preview for %s" % mesh.name)
        deactivated.append(modifier)
        modifier.show_viewport = False
      #endif
    #endfor

    mesh_copy = mesh.to_mesh(bpy.context.scene, True, 'PREVIEW')

    for modifier in deactivated:
      self.__logger.debug("__buildMesh: reactivating preview for %s" % mesh.name)
      modifier.show_viewport = True
    #endfor

    self.__logger.debug("__buildMeshCopyWithModifiersAppliedAndTriangulated: triangulating %s (%s)" % (mesh.name, mesh_copy))

    bm = bmesh.new()
    bm.from_mesh(mesh_copy)
    bmesh.ops.triangulate(bm, faces=bm.faces)

    return SMFTriangulatedInputMesh(self.__logger, mesh.vertex_groups, mesh_copy, bm)
  #end

  #
  # Build an SMF mesh from a triangulated mesh.
  #

  def __buildSMFMeshFromTriangulated(self, input_mesh):
    assert type(input_mesh) == SMFTriangulatedInputMesh

    mesh = input_mesh.b_mesh
    assert type(mesh) == bmesh.types.BMesh
    smf_mesh = SMFMesh(self.__logger, len(mesh.verts))

    for face in mesh.faces:
      assert type(face) == bmesh.types.BMFace

      assert len (face.verts) == 3
      face_vertex_0 = face.verts[0]
      assert type(face_vertex_0) == bmesh.types.BMVert
      face_vertex_1 = face.verts[1]
      assert type(face_vertex_1) == bmesh.types.BMVert
      face_vertex_2 = face.verts[2]
      assert type(face_vertex_2) == bmesh.types.BMVert

      v0 = SMFVertex()
      v0.position = self.__transformTranslationToExport(face_vertex_0.co)
      v0.normal   = self.__transformTranslationToExport(face_vertex_0.normal)

      v1 = SMFVertex()
      v1.position = self.__transformTranslationToExport(face_vertex_1.co)
      v1.normal   = self.__transformTranslationToExport(face_vertex_1.normal)

      v2 = SMFVertex()
      v2.position = self.__transformTranslationToExport(face_vertex_2.co)
      v2.normal   = self.__transformTranslationToExport(face_vertex_2.normal)

      #
      # UV coordinate information is stored "per-loop". The Blender documentation
      # states that a "loop" stores per-face-vertex information: Information
      # that is specific to a vertex when it appears in a specific face. In
      # Blender, a vertex may have one set of UV coordinates in one face,
      # and a completely different set of UV coordinates in another. Because
      # OpenGL wants one set of UV coordinates per vertex, it's necessary to
      # duplicate vertices. The duplication is handled when the vertex is
      # added to the SMFMesh structure.
      #

      for uv_name in mesh.loops.layers.uv.keys():
        uv_layer = mesh.loops.layers.uv[uv_name]

        assert (len(face.loops) == 3)
        face_loop_0 = face.loops[0]
        assert type(face_loop_0) == bmesh.types.BMLoop
        face_loop_1 = face.loops[1]
        assert type(face_loop_1) == bmesh.types.BMLoop
        face_loop_2 = face.loops[2]
        assert type(face_loop_2) == bmesh.types.BMLoop

        # Failing to make copies of the UV coordinates here will result
        # in accessing freed memory when the BMesh is later freed.
        uv0 = mathutils.Vector(face_loop_0[uv_layer].uv)
        uv1 = mathutils.Vector(face_loop_1[uv_layer].uv)
        uv2 = mathutils.Vector(face_loop_2[uv_layer].uv)

        self.__logger.debug("__buildSMFMeshFromTriangulated: uv[%d]: %s" % (face_vertex_0.index, uv0))
        self.__logger.debug("__buildSMFMeshFromTriangulated: uv[%d]: %s" % (face_vertex_1.index, uv1))
        self.__logger.debug("__buildSMFMeshFromTriangulated: uv[%d]: %s" % (face_vertex_2.index, uv2))

        v0.uv[uv_name] = uv0
        v1.uv[uv_name] = uv1
        v2.uv[uv_name] = uv2
      #endif

      #
      # Insert weights for all vertex groups to which the vertices belong.
      # If a vertex is not in a particular group, then it gets a weight of 0.0.
      #

      for group_name in input_mesh.vertex_groups.keys():
        group = input_mesh.vertex_groups[group_name]
        assert type(group) == bpy.types.VertexGroup

        #
        # The Blender weight() function raises RuntimeError if the given vertex
        # is not in the group.
        #

        try:
          v0.groups[group_name] = group.weight(face_vertex_0.index)
        except RuntimeError:
          v0.groups[group_name] = 0.0
        #endtry

        try:
          v1.groups[group_name] = group.weight(face_vertex_1.index)
        except RuntimeError:
          v1.groups[group_name] = 0.0
        #endtry

        try:
          v2.groups[group_name] = group.weight(face_vertex_2.index)
        except RuntimeError:
          v2.groups[group_name] = 0.0
        #endtry
      #endfor

      new_index0 = smf_mesh.addVertex(face_vertex_0.index, v0)
      assert type(new_index0) == int
      new_index1 = smf_mesh.addVertex(face_vertex_1.index, v1)
      assert type(new_index1) == int
      new_index2 = smf_mesh.addVertex(face_vertex_2.index, v2)
      assert type(new_index2) == int

      assert new_index0 < len(smf_mesh.vertices)
      assert new_index1 < len(smf_mesh.vertices)
      assert new_index2 < len(smf_mesh.vertices)

      smf_mesh.addTriangle(SMFTriangle(new_index0, new_index1, new_index2))
    #endfor

    return smf_mesh
  #end

  #
  # Produce an SMFMesh from a mesh object.
  #

  def __buildMesh(self, mesh):
    assert type(mesh) == bpy_types.Object
    assert mesh.type == 'MESH'

    input_mesh = self.__buildMeshCopyWithModifiersAppliedAndTriangulated(mesh)
    assert type(input_mesh) == SMFTriangulatedInputMesh

    smf = self.__buildSMFMeshFromTriangulated(input_mesh)
    input_mesh.free()
    return smf
  #end

  #
  # Serialize the SMFMesh to the SMF/T format.
  #

  def __writeMesh(self, out_file, smf_mesh):
    assert type(out_file) == io.TextIOWrapper
    assert type(smf_mesh) == SMFMesh

    out_file.write("attribute \"POSITION\" float 3 32\n")
    out_file.write("attribute \"NORMAL\" float 3 32\n")

    for uv in smf_mesh.uv_attributes:
      out_file.write("attribute \"UV:%s\" float 2 32\n" % uv)
    #endfor

    for group in smf_mesh.groups:
      out_file.write("attribute \"GROUP:%s\" float 1 32\n" % group)
    #endfor

    triangle_count = len(smf_mesh.triangles)
    vertex_count = len(smf_mesh.vertices)

    out_file.write("vertices %d\n" % vertex_count)
    out_file.write("triangles %d %d\n" % (triangle_count, smf_mesh.triangles_index_size))
    out_file.write("coordinates +x +y -z counter-clockwise\n")
    out_file.write("end\n")

    out_file.write("vertices-noninterleaved\n")
    out_file.write("attribute \"POSITION\"\n")
    for vertex in smf_mesh.vertices:
      assert type(vertex) == SMFVertex
      out_file.write("%.15f %.15f %.15f\n" % (vertex.position.x, vertex.position.y, vertex.position.z))
    #endfor

    out_file.write("attribute \"NORMAL\"\n")
    for vertex in smf_mesh.vertices:
      assert type(vertex) == SMFVertex
      out_file.write("%.15f %.15f %.15f\n" % (vertex.normal.x, vertex.normal.y, vertex.normal.z))
    #endfor

    for uv in smf_mesh.uv_attributes:
      out_file.write("attribute \"UV:%s\"\n" % uv)
      for vertex in smf_mesh.vertices:
        assert type(vertex) == SMFVertex
        out_file.write("%.15f %.15f\n" % (vertex.uv[uv].x, vertex.uv[uv].y))
      #endfor
    #endfor

    for group in smf_mesh.groups:
      out_file.write("attribute \"GROUP:%s\"\n" % group)
      for vertex in smf_mesh.vertices:
        assert type(vertex) == SMFVertex
        out_file.write("%.15f\n" % vertex.groups[group])
      #endfor
    #endfor
    out_file.write("end\n")

    out_file.write("triangles\n")
    for triangle in smf_mesh.triangles:
      assert type(triangle) == SMFTriangle
      out_file.write("%d %d %d\n" % (triangle.vertex0, triangle.vertex1, triangle.vertex2))
    #endfor
    out_file.write("end\n")

    meta = ""
    meta += "time: " + datetime.datetime.now(datetime.timezone.utc).strftime("%Y-%m-%dT%H:%M:%S%z") + "\n"
    meta += "app.role: author\n"
    meta += "app.version: Blender " + bpy.app.version_string + "\n"
    meta += "smf.version: ${parsedVersion.majorVersion}.${parsedVersion.minorVersion}.${parsedVersion.incrementalVersion}\n"

    out_file.write("metadata com.io7m.smf.application 1 0 1\n")
    out_file.write(base64.urlsafe_b64encode(bytearray(meta, encoding="utf-8")).decode(encoding="utf-8", errors="strict"))
    out_file.write("\n")
    out_file.write("end\n")
  #end

  #
  # Produce an SMFMesh and serialize it.
  #

  def __writeFile(self, out_file, mesh):
    assert type(out_file) == io.TextIOWrapper
    assert type(mesh) == bpy_types.Object
    assert mesh.type == 'MESH'

    out_file.write("smf 1 0\n")

    out = self.__buildMesh(mesh)
    assert type(out) == SMFMesh

    self.__logger.debug("__writeFile: type %s" % type(mesh))
    self.__writeMesh(out_file, out)
  #end

  #
  # The main entry point. Serialize the selected mesh object to a temporary
  # file. Maintain a log file, fail if any error messages have been logged,
  # and then atomically rename the temporary file to the given output path
  # if no errors have occurred.
  #

  def write(self, path):
    assert type(path) == str

    if not os.path.exists(path):
      os.mkdir(path)
    else:
      if not os.path.isdir(path):
        raise SMFExportFailed("Not a directory: %s" % path)
      #endif
    #endif

    meshes = []
    if (self.__selection == 'EXPORT_ALL'):
      for obj in bpy.data.objects:
        if obj.type == 'MESH':
          meshes.append(obj)
        #endif
      #endfor
    elif (self.__selection == 'EXPORT_SELECTED'):
      for obj in bpy.context.selected_objects:
        if obj.type == 'MESH':
          meshes.append(obj)
        #endif
      #endfor
    else:
      assert False, ("Unknown selection type: " + self.__selection)
    #endif

    for mesh in meshes:
      mesh_path     = os.path.join(path, bpy.path.clean_name(mesh.name) + ".smft")
      mesh_path_tmp = os.path.join(path, bpy.path.clean_name(mesh.name) + ".smft.tmp")
      mesh_log      = os.path.join(path, bpy.path.clean_name(mesh.name) + ".log")

      with open(mesh_log, "wt") as error_file:
        self.__logger = SMFLogger(self.__logger_severity, error_file)
        t = datetime.datetime.now()
        self.__logger.info("Export started at %s" % t.isoformat())
        self.__logger.info("File: %s" % mesh_path)
        self.__logger.info("Log:  %s" % mesh_log)

        self.__logger.debug("write: opening: %s" % mesh_path_tmp)
        with open(mesh_path_tmp, "wt") as out_file:
          self.__writeFile(out_file, mesh)

          errors = self.__logger.counts[SMF_LOG_MESSAGE_ERROR]
          if errors > 0:
            self.__logger.error("Export failed with %d errors." % errors)
            raise SMFExportFailed("Exporting failed due to errors.\nSee the log file at: %s" % mesh_log)
          else:
            self.__logger.info("Exported successfully")
            os.rename(mesh_path_tmp, mesh_path)
          #endif
        #endwith
      #endwith
    #endfor

#endclass
