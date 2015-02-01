unit uWorkThread;

interface

uses
  Classes, SysUtils, Dialogs;

const
  FEXT = '.a1x';

type
  TWorkThread = class(TThread)
  private
    WNFiles : TStrings;
    WCFiles : TStrings;
    WRevision : integer;
    WSrcDir : string;
    WOutDir : string;
    WClientName : string;
    UClientData : string;
    FSource: TStream;

    in_total, out_total : Int64;
    STemp : string;
  protected
    procedure Execute; override;
    procedure UpdateProgress;
    procedure UpdateStatus;
    procedure PackFiles(Dir: string; UploadDir: string; FList: Tstrings);
    procedure EnableForm;

    procedure CompressFile(const ASource, ADest: TFileName);
    procedure DoCompressStream(ins, outs : TStream);
    procedure CreateList;
    function MakeListRow(fpath, fname : string) : string;
  public
    property RevisionNum : integer write WRevision;
    property UpdaterClienName : string write WClientName;
    property SourceDir : string write WSrcDir;
    property OutputDir : string write WOutDir;
    property NormalFiles : TStrings write WNFiles;
    property CriticalFiles : TStrings write WCFiles;
  end;

implementation

uses
  uMain, Misc, md5, my_crc32, lzo;

function TWorkThread.MakeListRow(fpath, fname : string) : string;
var
  crc : TCRC;
  insz, outsz : Int64;
begin
  crc := TCRC.Create;
  crc.UpdateFile(fpath);

  insz :=  GetFileSize(fpath);
  outsz := GetFileSize(WOutDir+'\'+fname+FEXT);

  in_total := in_total + insz;
  out_total := out_total + outsz;

  Result :=

  fname+'|'+                                      // name
  crc.GetDigestStr+'|'+                           // crc32
//  md5.MD5Print(md5.MD5File(fpath))+'|'+           // md5
  IntToStr(insz) + '|' +                          // orig size
  IntToStr(outsz);                                // packed size

  crc.Free;
end;

{ WorkThread }

procedure TWorkThread.CompressFile(const ASource, ADest: TFileName);
var
  Source, Dest: TStream;
begin
  Source := TFileStream.Create(ASource, fmOpenRead + fmShareDenyWrite);
  try
    Dest := TFileStream.Create(ADest, fmCreate);
    try
      DoCompressStream(Source, Dest);
    finally
      Dest.Free;
    end;
  finally
    Source.Free;
  end;
end;

procedure TWorkThread.CreateList;
var
  LstFile : Textfile;
  i : integer;
begin
STemp := 'Create files list...';
Synchronize(UpdateStatus);

AssignFile(LstFile,WOutDir+'\files.lst');
Rewrite(LstFile);
WriteLn(LstFile,'[settings]');
WriteLn(LstFile,'Rev|'+IntToStr(WRevision));
WriteLn(LstFile,'[/settings]');
WriteLn(LstFile,'[self]');
WriteLn(LstFile,UClientData);
WriteLn(LstFile,'[/self]');

WriteLn(LstFile,'[files_critical]');
  for i:=0 to WCFiles.Count-1 do
  begin
    WriteLn(LstFile,WCFiles[i]);
  end;
WriteLn(LstFile,'[/files_critical]');

WriteLn(LstFile,'[files_normal]');
  for i:=0 to WNFiles.Count-1 do
  begin
    WriteLn(LstFile,WNFiles[i]);
  end;
WriteLn(LstFile,'[/files_normal]');

CloseFile(LstFile);
CompressFile(WOutDir+'\files.lst',WOutDir+'\files.lst'+FEXT);
DeleteFile(WOutDir+'\files.lst');
end;

procedure stream_write_int4(i : Longint; astream : tstream);
begin
  astream.write(i, sizeof(Longint));
end;

procedure TWorkThread.DoCompressStream(ins, outs : TStream);
var
  inb, outb : Pointer;
  insize, outsize : Integer;
begin
  insize := ins.Size;
  inb := GetMemory( insize );
  ins.ReadBuffer( inb^, insize );

  CompressData( inb, insize, outb, outsize );

  FreeMemory(inb);

   // записываем оригинальный размер
  stream_write_int4( insize, outs );
 // пишем сжатые данные
  outs.WriteBuffer( outb^, outsize );
  FreeMemory(outb);
end;

procedure TWorkThread.EnableForm;
begin
  fMain.btnBuild.Enabled:=True;
  fMain.Gauge1.Progress:=0;
//  fMain.Gauge1.Visible:=False;
  fMain.lblStatus.Caption := 'Complete (rate: '+IntToStr(Round(out_total / in_total * 100))+'%)';
//  ShowMessage(' Complete ! ');
end;

procedure TWorkThread.Execute;
var
  WNptr : ^TStringList;
  WCptr : ^TStringList;
  ClientPath : string;
begin
 WNptr:= @WNFiles;
 WCptr:= @WCFiles;
 ClientPath:= WSrcDir+'\'+WClientName;

 if (FileExists(ClientPath)) then
 begin
    STemp := WClientName;
    Synchronize(UpdateStatus);
    CompressFile(ClientPath,WOutDir+'\'+WClientName+FEXT);

    UClientData:=MakeListRow(ClientPath, WClientName);
 end;
 PackFiles(WSrcDir , WOutDir, WNptr^);
 PackFiles(WSrcDir , WOutDir, WCptr^);
 CreateList;
 WNFiles.Clear;
 WCFiles.Clear;
 Synchronize(EnableForm);
end;

procedure TWorkThread.PackFiles(Dir, UploadDir: string; FList: Tstrings);
var
  FileN,ZipName,hash: string;
  ZipSize,i :integer;
begin
  if Dir <> '' then
  begin
    if Dir[length(Dir)] <> '\' then Dir := Dir + '\';

    for i:=0 to FList.Count-1 do
    begin

      FileN:= ReplaceStr(Dir + FList[i],Dir,UploadDir+'\');

      ZipName:= FileN+FEXT;

      if(not DirectoryExists(ExtractFilePath(ZipName))) then
            ForceDirectories(ExtractFilePath(ZipName));

      CompressFile(Dir + FList[i],ZipName);
//      ZipSize:=GetFileSize(ZipName);
      STemp := FList[i];
      Synchronize(UpdateProgress);

      FList[i] := MakeListRow(Dir + FList[i], FList[i])
//      FList[i]:=ReplaceStr(FileN,UploadDir+'\','')+'|'+hash+'|'+IntToStr(ZipSize);
    end;
   end;
end;

procedure TWorkThread.UpdateProgress;
begin
  fMain.lblStatus.Caption := STemp;
  fMain.Gauge1.Progress := fMain.Gauge1.Progress + 1;
end;

procedure TWorkThread.UpdateStatus;
begin
  fMain.lblStatus.Caption := STemp;
end;

end.
