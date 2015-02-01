unit a1_blender_loader;

interface

uses
  CoreX, xmd, a1_java;

const
  OUT_DIR = 'out/';
  
type
  TBlendWeight = record
    bone_name : string;
    bone_index : Integer;
    w : Single;
    class operator Equal(const a, b: TBlendWeight): Boolean;
  end;

  TBlendVertex = record
    pos : TVec3f;
    normal : TVec3f;
    tx, ty : Single;
    w1, w2 : TBlendWeight;
    joint : TJoint;

    class operator Equal(const a, b: TBlendVertex): Boolean;
  end;

  TBlendFace = record
    i1, i2, i3 : Integer;
  end;



procedure ConvertBlender(fname : string);
procedure load_mesh(ss : TStream);
procedure load_skeleton(ss : TStream);
procedure load_anims(ss : TStream);
procedure check_mesh(mesh : TNodeMesh);


function read_matrix(ss : TStream) : TMat4f;
function ConvMatrixBlender(const M: TMat4f): TMat4f;

var
  // имя файла. который обрабатываем. без рашсишения
  blend_filename : string;

implementation

function get_filename(fname : string) : string;
begin
  Result := ExtractFileName(fname);
  while (Length(Result) > 0) and (Result[Length(Result)] <> '.') do
    Delete(Result, Length(Result), 1);
 
  if Result[Length(Result)] = '.' then Delete(Result, Length(Result), 1);  
end;

procedure ConvertBlender(fname : string);
var
  ss : TStream;
  fs : TStream;
  data : Pointer;
  size : LongInt;
  b : byte;
begin
  blend_filename := get_filename(fname);

  fs := TStream.Init(fname);
  size := fs.Size;
  data := GetMemory(Size);
  fs.Read(data^, Size);
  fs.Free;

  ss := TStream.Init(data, size);

  UpAxis := uaZ;
  UnitScale := 1;

  // признак наличия меша
  ss.Read(b, SizeOf(b));
  if b > 0 then
    load_mesh(ss);

  UpAxis := uaZ;
  // признак наличия скелета
  ss.Read(b, SizeOf(b));
  if b > 0 then
    load_skeleton(ss);

  // признак наличия анимаций
  ss.Read(b, SizeOf(b));
  if b > 0 then
    load_anims(ss);

  ss.Free;
  FreeMem(data);

//  ss := TStream.Init(fname);
//  load_mesh(ss);
//  ss.Free;


end;

procedure load_mesh(ss : TStream);
var
  F_Count , V_Count: LongInt;
  i : Integer;
  Vert : array of TBlendVertex;
  Faces : array of TBlendFace;
  jname : TStrArray;

  src : TSourceArray;
  mesh : TNodeMesh;
  str : string;

  v1, v2, v3 : TBlendVertex;
  face : TBlendFace;

  function add_bone(bn : string) : integer;
  var
    bbi : Integer;
  begin
    for bbi := 0 to Length(jname) - 1 do
      if jname[bbi] = bn then
      begin
        Result := bbi;
        exit;
      end;

    Result := Length(jname);
    SetLength(jname, Result + 1);
    jname[Result] := bn;
  end;

  function add_face(fi1, fi2, fi3 : Integer) : Integer;
  begin
    Result := Length(Faces);
    SetLength(Faces, Result + 1);
    Faces[Result].i1 := fi1;
    Faces[Result].i2 := fi2;
    Faces[Result].i3 := fi3;
  end;


  function add_vertex(v : TBlendVertex) : Integer;
  var
    vi : Integer;
  begin
    for vi := 0 to Length(vert) - 1 do
      if Vert[vi] = v then begin
        Result := vi;
        Exit;
      end;

    Result := Length(vert);
    SetLength(vert, Result + 1);
    Vert[Result] := v;
  end;

  function read_vertex() : TBlendVertex;
  var
    wc : word;
    wi : Integer;
    wd : TBlendWeight;
    w: Single;
  begin
    ss.Read(Result.pos.x, SizeOf(Single));
    ss.Read(Result.pos.y, SizeOf(Single));
    ss.Read(Result.pos.z, SizeOf(Single));

    ss.Read(Result.normal.x, SizeOf(Single));
    ss.Read(Result.normal.y, SizeOf(Single));
    ss.Read(Result.normal.z, SizeOf(Single));

    Result.w1.bone_name := ''; Result.w1.w := 0; Result.w1.bone_index := 0;
    Result.w2.bone_name := ''; Result.w2.w := 0; Result.w2.bone_index := 0;
    // weight data
    ss.Read(wc, SizeOf(wc));
    for wi := 0 to wc - 1 do
    begin
      wd.bone_name := ss.ReadAnsi;
      ss.Read(wd.w, SizeOf(wd.w));

      if wd.w > EPS then
      if wd.w > Result.w1.w then
      begin
        Result.w1 := wd;
        Result.w2 := Result.w1;
      end else

      if wd.w > Result.w2.w then
      begin
        Result.w2 := wd;
      end;
    end;

    if Result.w1.bone_name <> '' then
      Result.w1.bone_index := add_bone(Result.w1.bone_name);
    if Result.w2.bone_name <> '' then
      Result.w2.bone_index := add_bone(Result.w2.bone_name);

    if wc = 1 then
      Result.w2.bone_index := Result.w1.bone_index;
    w := 1 - (Result.w1.w + Result.w2.w);
    Result.joint.Weight :=  Result.w1.w + (w * 0.5);
    Result.joint.Joint[0] := Result.w1.bone_index;
    Result.joint.Joint[1] := Result.w2.bone_index;

    ss.Read(Result.tx, SizeOf(Single));
    ss.Read(Result.ty, SizeOf(Single));
  end;

begin
  // header
  str := ss.ReadAnsi;
  if str <> 'a1mesh' then
  begin
    Writeln('wrong header!');
    Exit;
  end;

  ss.Read(F_Count, SizeOf(F_Count));

  SetLength(Vert, 0);
  SetLength(Faces, 0);
  SetLength(jname, 0);
  writeln('read faces : ' , F_Count);

  // read faces
  for i := 0 to F_Count - 1 do
  begin
    // read 3 vertex
    v1 := read_vertex;
    v2 := read_vertex;
    v3 := read_vertex;

    add_face( add_vertex(v1), add_vertex(v2), add_vertex(v3) );
  end;

  F_Count := Length(Faces);
  V_Count := Length(Vert);
  Writeln('read done');
  writeln('actual verts : ' , Length(Vert));


  FillChar(src, SizeOf(src), 0);
  SetLength(src[SID_POSITION].ValueF, V_Count * 3);
  SetLength(src[SID_NORMAL].ValueF, V_Count * 3);
  SetLength(src[SID_TEXCOORD0].ValueF, V_Count * 2);
  SetLength(src[SID_JOINT].ValueJ, V_Count);

  SetLength(src[SID_POSITION].ValueI, F_Count * 3);
  SetLength(src[SID_NORMAL].ValueI, F_Count * 3);
  SetLength(src[SID_TEXCOORD0].ValueI, F_Count * 3);
  SetLength(src[SID_JOINT].ValueI, F_Count * 3);

  src[SID_POSITION].Stride := 3;
  src[SID_POSITION].Offset := 0;
  src[SID_POSITION].SourceURL := '123';
  src[SID_NORMAL].Stride := 3;
  src[SID_NORMAL].Offset := 0;
  src[SID_NORMAL].SourceURL := '123';
  src[SID_TEXCOORD0].Stride := 2;
  src[SID_TEXCOORD0].Offset := 0;
  src[SID_TEXCOORD0].SourceURL := '123';
  src[SID_JOINT].Stride := 1;
  src[SID_JOINT].Offset := 0;
  src[SID_JOINT].SourceURL := '123';

  for i := 0 to V_Count - 1 do begin
    src[SID_POSITION].ValueF[i*3] := vert[i].pos.x;
    src[SID_POSITION].ValueF[i*3+1] := vert[i].pos.y;
    src[SID_POSITION].ValueF[i*3+2] := vert[i].pos.z;
    src[SID_NORMAL].ValueF[i*3] := vert[i].normal.x;
    src[SID_NORMAL].ValueF[i*3+1] := vert[i].normal.y;
    src[SID_NORMAL].ValueF[i*3+2] := vert[i].normal.z;

    src[SID_TEXCOORD0].ValueF[i*2] := vert[i].tx;
    src[SID_TEXCOORD0].ValueF[i*2+1] := vert[i].ty;

    src[SID_JOINT].ValueJ[i] := vert[i].joint;
  end;

  for i := 0 to F_Count - 1 do begin
    src[SID_POSITION].ValueI[i*3] := faces[i].i1;
    src[SID_POSITION].ValueI[i*3+1] := faces[i].i2;
    src[SID_POSITION].ValueI[i*3+2] := faces[i].i3;

    src[SID_NORMAL].ValueI[i*3] := faces[i].i1;
    src[SID_NORMAL].ValueI[i*3+1] := faces[i].i2;
    src[SID_NORMAL].ValueI[i*3+2] := faces[i].i3;

    src[SID_TEXCOORD0].ValueI[i*3] := faces[i].i1;
    src[SID_TEXCOORD0].ValueI[i*3+1] := faces[i].i2;
    src[SID_TEXCOORD0].ValueI[i*3+2] := faces[i].i3;

    src[SID_JOINT].ValueI[i*3] := faces[i].i1;
    src[SID_JOINT].ValueI[i*3+1] := faces[i].i2;
    src[SID_JOINT].ValueI[i*3+2] := faces[i].i3;
  end;

  src[SID_JOINT].ValueS := jname;

  FillChar(mesh, SizeOf(mesh), 0);
//  mesh := TNodeMesh.Create;
  mesh.TexCoordCollada := False;
  mesh.Compile(src);
  check_mesh(mesh);
  Mesh.SaveA1(OUT_DIR+blend_filename);
  Mesh.Free;
end;

procedure check_mesh(mesh : TNodeMesh);
var
  i : Integer; 
  minx, miny, maxx, maxy : single;
begin
  minx := 0;
  miny := 0;
  maxx := 0;
  maxy := 0;
  for i := 0 to Length(mesh.Vertex) - 1 do
  begin
    if mesh.Vertex[i].TexCoord[0].x > maxx then
      maxx := mesh.Vertex[i].TexCoord[0].x;
    if mesh.Vertex[i].TexCoord[0].x < minx then
      minx := mesh.Vertex[i].TexCoord[0].x;

    if mesh.Vertex[i].TexCoord[0].y > maxy then
      maxy := mesh.Vertex[i].TexCoord[0].y;
    if mesh.Vertex[i].TexCoord[0].y < miny then
      miny := mesh.Vertex[i].TexCoord[0].y;
  end;

  Writeln('uv: ', minx, maxx, miny, maxy);
    
end;

procedure load_skeleton(ss : TStream);
var
  str : string;
  bi, i, j : Integer;
  b_count : word;
  w : word;
  b: Byte;

  Joint : array of record
    Id        : string;
    ParentId  : string;
    Parent    : TJointIndex;
    Bind      : TDualQuat;
    Frame     : TDualQuat;
    Name      : string;
  end;

  m, m1 : TMat4f;
begin
  // header
  str := ss.ReadAnsi;
  if str <> 'a1skeleton' then
  begin
    Writeln('wrong header!');
    Exit;
  end;

  ss.Read(b_count, SizeOf(b_count));
  SetLength(Joint, b_count);

  for bi := 0 to b_count - 1 do
  with Joint[bi] do
  begin
    ss.Read(b, SizeOf(b));
    if b = 1 then Continue; // skip flag
      
    id := ss.ReadAnsi;
    ParentId := ss.ReadAnsi;

    m1 := read_matrix(ss);
    m1 := ConvMatrixBlender(m1);  // bind

    m := read_matrix(ss);
    m := ConvMatrixBlender(m); // frame

    Bind := DualQuat(M1.Rot, M1.Pos);
    Frame := DualQuat(M.Rot, M.Pos);
  end;

  // recalc parent indices
  for i := 0 to Length(Joint) - 1 do
    if Joint[i].ParentId = '' then
      Joint[i].Parent := -1
    else
      for j := 0 to Length(Joint) - 1 do
        if Joint[j].Id = Joint[i].ParentId then
        begin
          Joint[i].Parent := j;
          break;
        end;

// info log
  Info(' Index'#9'Parent'#9'Bind'#9'Joint Name');
  for i := 0 to Length(Joint) - 1 do
    if Joint[i].Parent <> -1 then
      Writeln(' ', i, #9, Joint[i].Parent, #9, '+', #9, Joint[i].Name)
    else
      Writeln(' ', i, #9, Joint[i].Parent, #9, '-', #9, Joint[i].Name);

// save to file
  ss := TStream.Init(OUT_DIR + blend_filename + '.ask', True);
  if ss <> nil then
  begin
    w := Length(Joint);
    ss.Write(w, SizeOf(w));

    for i := 0 to Length(Joint) - 1 do
      ss.WriteAnsi(AnsiString(Joint[i].id));

    for i := 0 to Length(Joint) - 1 do
    begin
      TJavaStream.WriteInt(ss, Joint[i].Parent);
      TJavaStream.WriteDualQuat(ss, Joint[i].Bind);
      TJavaStream.WriteDualQuat(ss, Joint[i].Frame);
    end;
    ss.Free;
  end;
end;

procedure load_anims(ss : TStream);
type
  TFrameFlag = (ffRotX, ffRotY, ffRotZ, ffPosX, ffPosY, ffPosZ);
var
  str : string;
  i : Integer;
//  b_count : word;
  anims_count : word;
  fcount, jcount : word;
  fps : word;

  Joint : array of record
      Name  : string;
      Frame : array of TMat4f;
    end;

  procedure load_anim();
  var
    i, j : Integer;
//    M : TMat4f;
    Stream  : TStream;
  Flag : array of set of TFrameFlag;
  flag_bytes : array of Byte;
  SData : array of Single;
  SCount : LongInt;
  Rot, nRot : TQuat;
  Pos, nPos : TVec3f;
//  frotpos : TextFile;
  b : byte;
  
          procedure AddData(x: Single);
          begin
            SData[SCount] := x;
            Inc(SCount);
          end;                      

          function flag2byte(idx : Integer) : Byte;
          begin
            Result := 0;
            if ffRotX in Flag[idx] then Result := Result + 1;
            if ffRotY in Flag[idx] then Result := Result + 2;
            if ffRotZ in Flag[idx] then Result := Result + 4;

            if ffPosX in Flag[idx] then Result := Result + 8;
            if ffPosY in Flag[idx] then Result := Result + 16;
            if ffPosZ in Flag[idx] then Result := Result + 32;
          end;
  begin
    str := ss.ReadAnsi;
    if (str <> 'a1anim') then
    begin
      Writeln('wrong header!');
      Exit;
    end;

    str := ss.ReadAnsi;
    ss.Read(fcount, SizeOf(fcount));
    ss.Read(fps, SizeOf(fps));
    ss.Read(jcount, SizeOf(jcount));

    SetLength(Joint, jcount);
    for i := 0 to jcount - 1 do
    begin
      Joint[i].Name := ss.ReadAnsi;
      SetLength(Joint[i].Frame, fcount);
    end;

    for i := 0 to fcount - 1 do
      for j := 0 to jcount - 1 do begin
        ss.Read(b, SizeOf(b));
        if b = 1 then
          Continue; // skip flag

        Joint[j].Frame[i] := ConvMatrixBlender(read_matrix(ss));
        end;


        Info(' FPS    : ' + Conv(FPS));
        Info(' Frames : ' + Conv(FCount));

      // save to file
        if FCount > 0 then
        begin
      //    DeleteFile('frotpos.txt');
      //    AssignFile(frotpos, 'frotpos.txt');
      //    rewrite(frotpos);

          if str = '' then str := blend_filename;
          
          Stream := TStream.Init(OUT_DIR + str + '.aan', True);
          if Stream <> nil then
          begin
            i := Length(Joint);
            TJavaStream.WriteInt(Stream, i);
      //      Stream.Write(i, SizeOf(i));
            TJavaStream.WriteInt(Stream, FCount);
      //      Stream.Write(FCount, SizeOf(FCount));
            TJavaStream.WriteInt(Stream, FPS);
      //      Stream.Write(FPS, SizeOf(FPS));

            for i := 0 to Length(Joint) - 1 do
              Stream.WriteAnsi(AnsiString(Joint[i].Name));

            SetLength(Flag, FCount);
            SetLength(flag_bytes, FCount);
            SetLength(SData, FCount * 6); // 6 = Rot & Pos
            for i := 0 to Length(Joint) - 1 do
            begin
              Rot := Quat(0, 0, 0, 1);
              Pos := Vec3f(0, 0, 0);
              SCount := 0;
              for j := 0 to FCount - 1 do
              begin
              // collect flags of changes
                Flag[j] := [];
                nRot := Joint[i].Frame[j].Rot;
                nPos := Joint[i].Frame[j].Pos;
                nRot := nRot.Normal;
                if nRot.w < 0 then // to reconstruct w in TAnimData.Create, w must be greater than 0
                  nRot := nRot * -1;
                if abs(nRot.x - Rot.x) > EPS then Flag[j] := Flag[j] + [ffRotX];
                if abs(nRot.y - Rot.y) > EPS then Flag[j] := Flag[j] + [ffRotY];
                if abs(nRot.z - Rot.z) > EPS then Flag[j] := Flag[j] + [ffRotZ];
                if abs(nPos.x - Pos.x) > EPS then Flag[j] := Flag[j] + [ffPosX];
                if abs(nPos.y - Pos.y) > EPS then Flag[j] := Flag[j] + [ffPosY];
                if abs(nPos.z - Pos.z) > EPS then Flag[j] := Flag[j] + [ffPosZ];

                flag_bytes[j] := flag2byte(j);
                Rot := nRot;
                Pos := nPos;

      //          Writeln(frotpos, format('[%d][%d] flag: %d rot: %f %f %f %f pos: %f %f %f', [i, j, flag_bytes[j],
      //            rot.x, rot.y, rot.z, rot.w, pos.x, pos.y, pos.z]));

              // collect changed data
                if ffRotX in Flag[j] then AddData(Rot.x);
                if ffRotY in Flag[j] then AddData(Rot.y);
                if ffRotZ in Flag[j] then AddData(Rot.z);
                if ffPosX in Flag[j] then AddData(Pos.x);
                if ffPosY in Flag[j] then AddData(Pos.y);
                if ffPosZ in Flag[j] then AddData(Pos.z);
              end;
              Stream.Write(flag_bytes[0], SizeOf(flag_bytes[0]) * FCount);
      //        Stream.Write(SCount, SizeOf(SCount));
              TJavaStream.WriteInt(Stream, SCount);

              for j := 0 to scount - 1 do
                TJavaStream.WriteFloat(Stream, sdata[j]);

      //        Stream.Write(SData[0], SizeOf(SData[0]) * SCount);
            end;
            Stream.Free;
          end;

      //    CloseFile(frotpos);
        end else
          Info(' no animation');
        
  end;
begin
  ss.Read(anims_count, SizeOf(anims_count));
  for i := 0 to anims_count - 1 do
  begin
    load_anim();
  end;
end;

{ TBlendVertex }

class operator TBlendVertex.Equal(const a, b: TBlendVertex): Boolean;
begin
  Result :=  (a.pos = b.pos) and
  (a.normal = b.normal) and
  (a.tx = b.tx) and (a.ty = b.ty) and
  (a.w1 = b.w1) and (a.w2 = b.w2)
end;

{ TBlendWeight }

class operator TBlendWeight.Equal(const a, b: TBlendWeight): Boolean;
begin
  Result := (a.bone_name = b.bone_name) and (a.w = b.w)
end;

function read_matrix(ss : TStream) : TMat4f;
begin
  ss.Read( result, sizeof(result) );
end;

function ConvMatrixBlender(const M: TMat4f): TMat4f;
const
  XM : TMat4f = (
    e00:  0; e10:  1; e20:  0; e30:  0;
    e01: -1; e11:  0; e21:  0; e31:  0;
    e02:  0; e12:  0; e22:  1; e32:  0;
    e03:  0; e13:  0; e23:  0; e33:  1;
  );
  ZM : TMat4f = (
    e00:  1; e10:  0; e20:  0; e30:  0;
    e01:  0; e11:  0; e21: -1; e31:  0;
    e02:  0; e12:  1; e22:  0; e32:  0;
    e03:  0; e13:  0; e23:  0; e33:  1;
  );
begin
  case UpAxis of
    uaX : Result := XM * M * XM.Inverse;
    uaZ : Result := ZM * M * ZM.Inverse;
  else
    Result := M;
  end;
  Result := Result.Transpose;
  Result.Pos := Result.Pos * UnitScale;
end;

end.
