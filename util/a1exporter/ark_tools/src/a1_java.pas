unit a1_java;

interface

uses
  CoreX;

type
  TJavaConvert = record
    procedure flip;
    case i : Integer of
      0 : (buf : array [0..3] of Byte);
      1 : (float : Single);
      2 : (int : Integer);
  end;

  TJavaConvert2 = record
    procedure flip;
    case i : Integer of
      0 : (buf : array [0..1] of Byte);
      1 : (w : word);
  end;

  TJavaStream = class
  public
    class procedure WriteFloat(ss : TStream; f : Single);
    class procedure WriteInt(ss : TStream; i : Integer);
    class procedure WriteWord(ss : TStream; w : Word);
    class procedure WriteVec2f(ss : TStream; v : tvec2f);
    class procedure WriteVec3f(ss : TStream; v : tvec3f);
    class procedure WriteVec4f(ss : TStream; v : tvec4f);
    class procedure WriteByte(ss : TStream; b : Byte);
    class procedure WriteBoolean(ss : TStream; b : Boolean);

    class procedure WriteDualQuat(ss : TStream; dq : TDualQuat);
    class procedure WriteQuat(ss: TStream; q : TQuat);
  end;

implementation

{ TJavaStream }

{ TJavaStream }

class procedure TJavaStream.WriteBoolean(ss: TStream; b: Boolean);
var
  bb : Byte;
begin
  if b then
    bb := 1
  else
    bb := 0;
  WriteByte(ss, bb);
end;

class procedure TJavaStream.WriteByte(ss: TStream; b: Byte);
begin
  ss.Write(b, SizeOf(b));
end;

class procedure TJavaStream.WriteDualQuat(ss: TStream; dq: TDualQuat);
begin
  WriteQuat(ss, dq.Real);
  WriteQuat(ss, dq.Dual);
end;

class procedure TJavaStream.WriteFloat(ss: TStream; f: Single);
var
  b : TJavaConvert;
begin
  b.float := f;
  b.flip;
  ss.Write(b.buf, 4);
end;

class procedure TJavaStream.WriteInt(ss: TStream; i: Integer);
var
  b : TJavaConvert;
begin
  b.int := i;
  b.flip;
  ss.Write(b.buf, 4);
end;

class procedure TJavaStream.WriteQuat(ss: TStream; q: TQuat);
begin
  WriteFloat(ss, q.x);
  WriteFloat(ss, q.y);
  WriteFloat(ss, q.z);
  WriteFloat(ss, q.w);
end;

class procedure TJavaStream.WriteVec2f(ss: TStream; v: tvec2f);
begin
  WriteFloat(ss, v.x);
  WriteFloat(ss, v.y);
end;

class procedure TJavaStream.WriteVec3f(ss: TStream; v: tvec3f);
begin
  WriteFloat(ss, v.x);
  WriteFloat(ss, v.y);
  WriteFloat(ss, v.z);
end;

class procedure TJavaStream.WriteVec4f(ss: TStream; v: tvec4f);
begin
  WriteFloat(ss, v.x);
  WriteFloat(ss, v.y);
  WriteFloat(ss, v.z);
  WriteFloat(ss, v.w);
end;

class procedure TJavaStream.WriteWord(ss: TStream; w: Word);
var
  b : TJavaConvert2;
begin
  b.w := w;
  b.flip;
  ss.Write(b.buf, 2);
end;

{ TJavaConvert }

procedure TJavaConvert.flip;
var
  b : Byte;
begin
  b := buf[0];
  buf[0] := buf[3];
  buf[3] := b;

  b := buf[1];
  buf[1] := buf[2];
  buf[2] := b;
end;

{ TJavaConvert2 }

procedure TJavaConvert2.flip;
var
  b : Byte;
begin
  b := buf[0];
  buf[0] := buf[1];
  buf[1] := b;
end;

end.
