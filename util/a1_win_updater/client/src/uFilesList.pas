unit uFilesList;

interface

uses
  Classes, SysUtils, Misc;

type
  TFilesList = class
  private
    LFile : textfile;
  public
    constructor Create(FName: string);
    destructor Destroy;
    function GetFSection(SName : string) : TStringList;
    function GetKeyValue(SName, Key : string) : string;
  end;


implementation

{ TFilesList }

constructor TFilesList.Create(FName: string);
begin
  AssignFile(LFile,FName);
end;

destructor TFilesList.Destroy;
begin
  CloseFile(LFile);
end;

function TFilesList.GetFSection(SName: string): TStringList;
var
  TS : string;
begin
  Result:=TStringList.Create;
  Reset(LFile);
  while not Eof(LFile) do
  begin
    ReadLn(LFile,TS);
    TS:=Trim(TS);
    if (TS = '['+SName+']') then
    begin
      repeat
        ReadLn(LFile,TS);
        TS:=Trim(TS);
        if(TS<>'') then Result.Add(TS);
      until (TS='[/'+SName+']');
      if(Result.Count>0) then Result.Delete(Result.Count-1);
      Break;
    end;
  end;
  if(Result.Count=0) then Result.Add('FAIL');
end;

function TFilesList.GetKeyValue(SName, Key: string): string;
var
  TS : string;
  Params : TStringList;
begin
  Reset(LFile);
  Result:='';
  Params:=TStringList.Create;
  while not Eof(LFile) do
  begin
    ReadLn(LFile,TS);
    TS:=Trim(TS);
    if (TS = '['+SName+']') then
    begin
      repeat
        ReadLn(LFile,TS);
        TS:=Trim(TS);
        if(TS<>'') then
        begin
          Params:=Tokenize(TS,'|');
          if(Params[Params.Count-1]=Key) then
          begin
            Result:=Params[0];
            break;
          end;
        end;
      until (TS='[/'+SName+']');
      Break;
    end;
  end;
end;

end.
