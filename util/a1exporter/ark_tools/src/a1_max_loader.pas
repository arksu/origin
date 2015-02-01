unit a1_max_loader;

interface

uses
  CoreX, xmd;

type
  TMaxVertex = record
    pos : TVec3f;
    normal : TVec3f;
  end;

  TMaxFace = record
    i1, i2, i3 : Integer;
  end;

  TMaxTCoord = record
    x, y : Single;
  end;

procedure Convert3dMax(fname : string);

implementation

procedure Convert3dMax(fname : string);
var
  ss : TStream;
  V_Count, F_Count, T_Count : LongInt;
  i : Integer;
  Vert : array of TMaxVertex;
  Faces : array of TMaxFace;
  FacesTV : array of TMaxFace;
  TexCoords : array of TMaxTCoord;

  src : TSourceArray;
  mesh : TNodeMesh;
begin
  ss := TStream.Init(fname);

  ss.Read(V_Count, SizeOf(V_Count));
  ss.Read(F_Count, SizeOf(F_Count));
  ss.Read(T_Count, SizeOf(T_Count));

  SetLength(Vert, V_Count);
  SetLength(Faces, F_Count);
  SetLength(FacesTV, F_Count);
  SetLength(TexCoords, T_Count);

  for i := 0 to V_Count - 1 do
  begin
    ss.Read(Vert[i].pos.x, SizeOf(Single));
    ss.Read(Vert[i].pos.y, SizeOf(Single));
    ss.Read(Vert[i].pos.z, SizeOf(Single));

    ss.Read(Vert[i].normal.x, SizeOf(Single));
    ss.Read(Vert[i].normal.y, SizeOf(Single));
    ss.Read(Vert[i].normal.z, SizeOf(Single));
  end;

  for i := 0 to F_Count - 1 do
  begin
    ss.Read(Faces[i].i1, SizeOf(integer));
    ss.Read(Faces[i].i2, SizeOf(integer));
    ss.Read(Faces[i].i3, SizeOf(integer));
  end;

  for i := 0 to T_Count - 1 do
  begin
    ss.Read(TexCoords[i].x, SizeOf(Single));
    ss.Read(TexCoords[i].y, SizeOf(Single));
  end;

  for i := 0 to F_Count - 1 do
  begin
    ss.Read(FacesTV[i].i1, SizeOf(integer));
    ss.Read(FacesTV[i].i2, SizeOf(integer));
    ss.Read(FacesTV[i].i3, SizeOf(integer));
  end;

  FillChar(src, SizeOf(src), 0);
  SetLength(src[SID_POSITION].ValueF, V_Count * 3);
  SetLength(src[SID_NORMAL].ValueF, V_Count * 3);
  SetLength(src[SID_TEXCOORD0].ValueF, V_Count * 2);
  SetLength(src[SID_POSITION].ValueI, F_Count * 3);
  SetLength(src[SID_NORMAL].ValueI, F_Count * 3);
  SetLength(src[SID_TEXCOORD0].ValueI, F_Count * 2);

  src[SID_POSITION].Stride := 3;
  src[SID_POSITION].Offset := 0;
  src[SID_POSITION].SourceURL := '123';
  src[SID_NORMAL].Stride := 3;
  src[SID_NORMAL].Offset := 0;
  src[SID_NORMAL].SourceURL := '123';
  src[SID_TEXCOORD0].Stride := 2;
  src[SID_TEXCOORD0].Offset := 0;
  src[SID_TEXCOORD0].SourceURL := '123';

  for i := 0 to V_Count - 1 do begin
    src[SID_POSITION].ValueF[i*3] := vert[i].pos.x;
    src[SID_POSITION].ValueF[i*3+1] := vert[i].pos.y;
    src[SID_POSITION].ValueF[i*3+2] := vert[i].pos.z;
    src[SID_NORMAL].ValueF[i*3] := vert[i].normal.x;
    src[SID_NORMAL].ValueF[i*3+1] := vert[i].normal.y;
    src[SID_NORMAL].ValueF[i*3+2] := vert[i].normal.z;
  end;
  for i := 0 to T_Count - 1 do begin
    src[SID_TEXCOORD0].ValueF[i*2] := TexCoords[i].x;
    src[SID_TEXCOORD0].ValueF[i*2+1] := TexCoords[i].y;
  end;
  for i := 0 to F_Count - 1 do begin
    src[SID_POSITION].ValueI[i*3] := faces[i].i1;
    src[SID_POSITION].ValueI[i*3+1] := faces[i].i2;
    src[SID_POSITION].ValueI[i*3+2] := faces[i].i3;
    src[SID_NORMAL].ValueI[i*3] := faces[i].i1;
    src[SID_NORMAL].ValueI[i*3+1] := faces[i].i2;
    src[SID_NORMAL].ValueI[i*3+2] := faces[i].i3;

    src[SID_TEXCOORD0].ValueI[i*3] := FacesTV[i].i1;
    src[SID_TEXCOORD0].ValueI[i*3+1] := FacesTV[i].i2;
    src[SID_TEXCOORD0].ValueI[i*3+2] := FacesTV[i].i3;
  end;


  ss.Free;
  FillChar(mesh, SizeOf(mesh), 0);
  UpAxis := uaZ;
  UnitScale := 1;
  mesh.Compile(src);
  Mesh.SaveA1('cache/' + 'test_a1max_mesh');
end;

end.
