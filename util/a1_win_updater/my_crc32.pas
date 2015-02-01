unit my_crc32;
{
  CRC32 by arksu
  27.01.2009
  base on crc32 unit from lzma (7zip)
}

interface
uses
  classes, sysutils;
const
  FILE_BUFFER_LENGTH = 2048; // in bytes

type TCRC=class
       public
         Value:integer;
         constructor Create;
         procedure Init;
         procedure Update(const data: array of byte;const offset,size:integer); overload;
         procedure Update(const data: array of byte); overload;
         procedure UpdateByte(const b:integer);
         function GetDigest:integer;

         // arksu
         procedure UpdateFile(fname : string);
         function  GetDigestStr: string;
       end;

implementation

var Table: array [0..255] of integer;

function data2hexstr(data : pointer; len : integer): string;
var
  i : integer;
  p : pbyte absolute data;
begin
  result := '';
  for i := 1 to len do
    begin
    result := result + inttohex(p^, 2);
    inc(p);
    end;
end;


constructor TCRC.Create;
begin
Value:=-1;
end;

procedure TCRC.Init;
begin
Value:=-1;
end;

procedure TCRC.Update(const data: array of byte;const offset,size:integer);
var i:integer;
begin
for i := 0 to size-1 do
    value := Table[(value xor data[offset + i]) and $FF] xor (value shr 8);
end;

procedure TCRC.Update(const data: array of byte);
var size:integer;
    i:integer;
begin
size := length(data);
for i := 0 to size - 1 do
    value := Table[(value xor data[i]) and $FF] xor (value shr 8);
end;

procedure TCRC.UpdateByte(const b:integer);
begin
value := Table[(value xor b) and $FF] xor (value shr 8);
end;

procedure TCRC.UpdateFile(fname: string);
var
  buf : array [0..FILE_BUFFER_LENGTH-1] of byte;
  fs :tfilestream;
  nCount : integer;
begin
  if not fileexists(fname) then exit;
  try
    fs := tfilestream.Create(fname, fmopenread or fmShareDenyWrite);
    repeat
      nCount := fs.Read(buf, FILE_BUFFER_LENGTH);
      Update(buf, 0, nCount);
    until nCount=0;
  finally
    if fs<>nil then fs.Free;
  end;
end;

function TCRC.GetDigest:integer;
begin
result:=value xor (-1);
end;

function TCRC.GetDigestStr: string;
var
  res : integer;
begin
  res := GetDigest;
  result := data2hexstr(@res, sizeof(res));
end;

procedure InitCRC;
var i,j,r:integer;
begin
for i := 0 to 255 do begin
    r := i;
    for j := 0 to 7 do begin
        if ((r and 1) <> 0) then
           r := (r shr 1) xor integer($EDB88320)
        else r := r shr 1;
        end;
    Table[i] := r;
    end;
end;

initialization
InitCRC;

end.
