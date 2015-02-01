// Вы любите изобретать велосипеды ? Я не люблю.
// Часть этих функций собрана по "интернетам"

unit Misc;

interface
uses
  Windows, SysUtils, Classes, ShellAPI, Forms;
  
function ReplaceStr(const S, Srch, Replace: string): string;
function StrSearch(StartPos: Integer; const S, P: string): Integer;
function Tokenize(Str: WideString; Delimiter: string): TStringList;
procedure GenKillerBat(MyNameIs: string);
procedure RunApp(Path: string);
function DelDir(dir: string): Boolean;

implementation

function DelDir(dir: string): Boolean;
var
  fos: TSHFileOpStruct;
begin
if (DirectoryExists(dir)) then begin
  ZeroMemory(@fos, SizeOf(fos));
  with fos do
  begin
    wFunc := FO_DELETE;
    fFlags := FOF_SILENT or FOF_NOCONFIRMATION;
    pFrom := PChar(dir + #0);
  end;
  Result := (0 = ShFileOperation(fos));
end;
end;

// Ф-я замены подстроки в строке
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

// процедура запуска приложения ( полный путь к "бинарнику" или "батнику" )
procedure RunApp(Path: string);
var
  p1: array[0..100] of Char;
//  w1: cardinal;
begin
  ChDir(ExtractFilePath(Path));
  StrPcopy(p1, ExtractFilePath(Path));
  if GetModuleHandle(p1) = 0 then
  begin
//    StrPcopy(p2, Path);
//    w1 := WinExec(@p2, SW_Restore);
//    w1 := WinExec(PAnsiChar(Path), SW_SHOWNORMAL);

    ShellExecute( Application.Handle, 'open', PChar(Path), nil, PChar(ExtractFilePath(Path)), SW_NORMAL );
  end;
end;

// ф-я поиска подстроки в строке
function StrSearch(StartPos: Integer; const S, P: string): Integer;
type
  TBMTable = array[0..255] of Integer;
var
  Pos, lp, i: Integer;
  BMT: TBMTable;
begin
  for i := 0 to 255 do
    BMT[i] := Length(P);
  for i := Length(P) downto 1 do
    if BMT[Byte(P[i])] = Length(P) then
      BMT[Byte(P[i])] := Length(P) - i;
  lp := Length(P);
  Pos := StartPos + lp - 1;
  while Pos <= Length(S) do
    if P[lp] <> S[Pos] then
      Pos := Pos + BMT[Byte(S[Pos])]
    else if lp = 1 then
    begin
      Result := Pos;
      Exit;
    end
    else
      for i := lp - 1 downto 1 do
        if P[i] <> S[Pos - lp + i] then
        begin
          Inc(Pos);
          Break;
        end
        else if i = 1 then
        begin
          Result := Pos - lp + 1;
          Exit;
        end;
  Result := 0;
end;

// ф-я разбиения строки на подстрок. возвращает TStringList с подстроками :)))
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



// мышь- камикадзе
procedure GenKillerBat(MyNameIs: string);
var BatchFile:     TextFile;
    BatchFileName: string;
begin
  BatchFileName := GetCurrentDir + '\Update.bat';
  AssignFile(BatchFile, BatchFileName);
  Rewrite(BatchFile);
  Writeln(BatchFile,'@echo off');
  Writeln(BatchFile, 'taskkill /IM ' + MyNameIs + ' /F');

  // pause у нас больше нет ... но всегда моно пару раз пнуть крюк :)
  Writeln(BatchFile,'ping 127.0.0.1 -n 3 > nul');

  if(FileExists(GetCurrentDir + '\'+ MyNameIs +'.New')=true) then begin
     Writeln(BatchFile, 'del "' + GetCurrentDir+'\files.lst"');
     Writeln(BatchFile, 'del "' + GetCurrentDir+'\' + MyNameIs + '"');
     Writeln(BatchFile,'rename ' + MyNameIs + '.New '+ MyNameIs);
  end;
  Writeln(BatchFile, 'start ' + MyNameIs);
  Writeln(BatchFile, 'del "' + ExtractFileName(BatchFileName) + '"');
  CloseFile(BatchFile);
end;

end.
