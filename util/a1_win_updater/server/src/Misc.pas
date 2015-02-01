// Вы любите изобретать велосипеды ? Я не люблю.
// Часть этих функций собрана по "интернетам"

unit Misc;

interface

uses
  Windows, SysUtils, Classes;

function ReplaceStr(const S, Srch, Replace: string): string;
function GetFileSize(Name: string) : integer;
function Tokenize(Str: WideString; Delimiter: string): TStringList;

implementation

function Tokenize(Str: WideString; Delimiter: string): TStringList;
var
  tmpStrList: TStringList;
  tmpString, tmpVal: WideString;
  DelimPos: LongInt;
begin
  tmpStrList := TStringList.Create;
  TmpString := Str;
  DelimPos := 1;
  while DelimPos > 0 do
  begin
    DelimPos := LastDelimiter(Delimiter, TmpString);
    tmpVal := Copy(TmpString, DelimPos + 1, Length(TmpString));
    if tmpVal <> '' then
      tmpStrList.Add(tmpVal);
    Delete(TmpString, DelimPos, Length(TmpString));
  end;
  Tokenize := tmpStrList;
end;

function ReplaceStr(const S, Srch, Replace: string): string;
var
  I: Integer;
  Source: string;
begin
  Source := S;
  Result := '';
  repeat
    I := Pos(Srch, Source);
    if I>0 then
    begin
      Result := Result + Copy(Source, 1, I - 1) + Replace;
      Source := Copy(Source, I + Length(Srch), MaxInt);
    end
    else
      Result := Result + Source;
  until I<= 0;
end;

// примитив ...
function GetFileSize(Name: String): Integer;
var
  FStream: TFileStream;
begin
  if(FileExists(Name)) then
    FStream:=TFileStream.Create(Name, fmOpenRead)
  else Result:=-1;
  if Result<>-1 then
  begin
    Result:=FStream.Size;
    FStream.Free;
  end;
end;

// Ф-я замены подстроки в строке




end.
