# -*- coding: utf-8 -*-
# Generated by the protocol buffer compiler.  DO NOT EDIT!
# source: request.proto
"""Generated protocol buffer code."""
from google.protobuf.internal import builder as _builder
from google.protobuf import descriptor as _descriptor
from google.protobuf import descriptor_pool as _descriptor_pool
from google.protobuf import symbol_database as _symbol_database
# @@protoc_insertion_point(imports)

_sym_db = _symbol_database.Default()




DESCRIPTOR = _descriptor_pool.Default().AddSerializedFile(b'\n\rrequest.proto\x12\toperation\"\xb4\x01\n\x07Request\x12=\n\roperationType\x18\x01 \x02(\x0e\x32 .operation.Request.OperationType:\x04NAME\x12\x0c\n\x04name\x18\x02 \x01(\t\x12\x0c\n\x04tile\x18\x03 \x01(\t\"N\n\rOperationType\x12\x08\n\x04NAME\x10\x00\x12\n\n\x06LEADER\x10\x01\x12\x07\n\x03NEW\x10\x02\x12\t\n\x05TILE1\x10\x03\x12\t\n\x05TILE2\x10\x04\x12\x08\n\x04QUIT\x10\x05\"\x13\n\x04Logs\x12\x0b\n\x03log\x18\x01 \x03(\t**\n\x07Message\x12\x0b\n\x07\x43ONNECT\x10\x00\x12\t\n\x05START\x10\x01\x12\x07\n\x03WIN\x10\x02\x42\x18\n\x07\x62uffersB\rRequestProtos')

_builder.BuildMessageAndEnumDescriptors(DESCRIPTOR, globals())
_builder.BuildTopDescriptorsAndMessages(DESCRIPTOR, 'request_pb2', globals())
if _descriptor._USE_C_DESCRIPTORS == False:

  DESCRIPTOR._options = None
  DESCRIPTOR._serialized_options = b'\n\007buffersB\rRequestProtos'
  _MESSAGE._serialized_start=232
  _MESSAGE._serialized_end=274
  _REQUEST._serialized_start=29
  _REQUEST._serialized_end=209
  _REQUEST_OPERATIONTYPE._serialized_start=131
  _REQUEST_OPERATIONTYPE._serialized_end=209
  _LOGS._serialized_start=211
  _LOGS._serialized_end=230
# @@protoc_insertion_point(module_scope)
