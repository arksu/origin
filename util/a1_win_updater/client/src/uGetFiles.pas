unit uGetFiles;

interface

uses
  Classes, Wininet, Windows, SysUtils, Dialogs, Forms;

const
  FEXT = '.a1x';

type
  TGetFilesThread = class(TThread)
  private
    LTemp : Longword;             // Временная переменная ... точнее так , переменная для временных данных
    STemp : string;               // ---//--- cтроковая
    FilesToGet : TStringList;     // Это будем качать
    FilesSize : Longword;         // Общий размер загружаемых файлов
    CBackup : integer;            // Флаг необходимости бэкапа
    CRevision : integer;          // Текущая ревизия
    CForceCheck : boolean;        // Флаг Принудительной проверки [ Если нажали на фулл чек или при различии ревизий ]
    CSwitch : integer;            // Бестолковый флаг , используется в ф-ии обновления прогресса
    UUrl : string;                // URL каталога с апдейтами
    USelfParam : string;          // Параметры клиента автообновления, используется в ф-ии самообновления
    Dir: string;                  // "Домашняя" папка [ текущий рабочий каталог клиента ]
    FSource: TStream;             // используется архиватором
  protected
    procedure Execute; override;
    procedure UpdateFileProgress;
    procedure SetFileProgressMax;
    procedure UpdateStatusLabel;
    procedure UpdateFileDecompStat;
    procedure UpdateFilesProgress;
    procedure CheckFiles(FList : TStringList);
    procedure BZProgress(Sender: TObject);
    procedure LockFMain;
    procedure UNLockFMain;
    procedure GetFiles;
    procedure SelfUpdate(SelfVal : string);
    procedure UpdateRevision;
    procedure DoUncompressStream(ins, outs: TStream);
    procedure DoUncompress(const ASource, ADest: TFileName);
    function HTTPGetFile(const fileURL, FileName: string; sh_progress: boolean): boolean;
  public
    property CreateBackup : integer write CBackup;
    property UpdatesUrl : string write UUrl;
    property LocalRevision : integer write CRevision;
    property ForceCheck : boolean write CForceCheck;
  end;


implementation

uses uMain, Misc, my_crc32, uFilesList, lzo;

var
  FilesList : TFilesList;

{ GFilesThread }

procedure TGetFilesThread.BZProgress(Sender: TObject);
begin
  LTemp:=FSource.Position;
  Synchronize(UpdateFileDecompStat);
end;

procedure TGetFilesThread.CheckFiles(FList: TStringList);
var
  i: integer;
  FParam: TStringList;
  FNameLocal: string;
  crc : TCRC;
begin
  if(FList.Count>0) and (FList[0]<>'FAIL') and (not terminated) then
  begin
    STemp:='Checking files';
    Synchronize(UpdateStatusLabel);
    CSwitch:=1;
    LTemp:=FList.Count-1;
    Synchronize(SetFileProgressMax);
    FParam:=TStringList.Create;
    for i:=0 to FList.Count-1 do
    begin
      LTemp:=i;
      Synchronize(UpdateFilesProgress);
      FParam:=Tokenize(FList[i],'|');
      FNameLocal:=Dir+FParam[3];
      STemp:='Checking '+FParam[3];
      Synchronize(UpdateStatusLabel);
      if (not FileExists(FNameLocal)) then
      begin
        FilesToGet.Add(FList[i]);
        FilesSize:=FilesSize+StrToInt(FParam[0]);
      end
      else
      begin
        crc := TCRC.Create;
        crc.UpdateFile(FNameLocal);
        if (crc.GetDigestStr <> FParam[2]) then
        begin
          FilesToGet.Add(FList[i]);
          FilesSize:=FilesSize+StrToInt(FParam[0]);
        end;
        crc.Free;
      end;
    end;
    FParam.Free;
    LTemp:=0;
    Synchronize(UpdateFilesProgress);
    STemp:='';
    Synchronize(UpdateStatusLabel);
  end;
end;

procedure TGetFilesThread.DoUncompress(const ASource, ADest: TFileName);
var
  Source, Dest: TStream;
begin
  Source := TFileStream.Create(ASource, fmOpenRead + fmShareDenyWrite);
  try
    Dest := TFileStream.Create(ADest, fmCreate);
    try
      DoUncompressStream(Source, Dest);
    finally
      Dest.Free;
    end;
  finally
    Source.Free;
    DeleteFile(ASource);
  end;
end;

function  stream_read_int4(astream : tstream) : Longint;
begin
  astream.read(result , sizeof(Longint));
end;

procedure TGetFilesThread.DoUncompressStream(ins, outs: TStream);
var
  inb, outb : Pointer;
  insize, outsize : Integer;
begin
  insize := ins.Size;
  // получаем оригинальный размер
  outsize := stream_read_int4(ins) + 1*1024*1024;
  // выделяем паять
  inb := GetMemory( insize );
  outb := GetMemory( outsize );
  // читаем данные
  ins.ReadBuffer( inb^, insize-4 );

  DecompressData( inb, insize, outb, outsize );

  FreeMemory(inb);
  outs.WriteBuffer( outb^, outsize );
  FreeMemory(outb);
end;

procedure TGetFilesThread.GetFiles;
var
  FParam : TStringList;
  i : integer;
  F,  error : boolean;
  LocalFile, BakFile: string;
begin
  error := False;
  if (FilesToGet.Count>0) then
  begin
    FParam:=TStringList.Create;
    LTemp:=FilesToGet.Count-1;
    CSwitch:=1;
    Synchronize(SetFileProgressMax);
    i:=0;
    while (i < FilesToGet.Count) and (not terminated) do
    begin
      // Отображаем прогресс загрузки файлов
      FParam:=Tokenize(FilesToGet[i],'|');
      LocalFile:= Dir+FParam[3];
      STemp:='Downloading '+ FParam[3];
      Synchronize(UpdateStatusLabel);

      // Устанавливаем Макс. Значения для прогресса загрузки файла
      CSwitch:=0;
      LTemp:= StrToInt(FParam[0]);
      Synchronize(SetFileProgressMax);

      if (not DirectoryExists(ExtractFilePath(LocalFile))) then
        ForceDirectories(ExtractFilePath(LocalFile));

      F:=HTTPGetFile(UUrl+ReplaceStr(FParam[3],'\','/')+FEXT,LocalFile+FEXT,True);
      if (F) then
      begin
        try
          if (CBackup=1) then
          begin
            BakFile:=Dir+'backup\'+FParam[3];

            if (not DirectoryExists(ExtractFilePath(BakFile))) then
              ForceDirectories(ExtractFilePath(BakFile));

            CopyFile(PChar(LocalFile),PChar(BakFile),false);
          end;
          STemp:='Extracting '+ FParam[3];
          Synchronize(UpdateStatusLabel);
          DoUncompress(LocalFile+FEXT,Dir+FParam[3]);
        except
          STemp:='Update Failed';
          error := True;
        end;
      end
      else
      begin
        STemp:='Update Failed';
        error := True;
        Break;
      end;
    inc(i);
    LTemp:=i;
    CSwitch:=1;
    Synchronize(UpdateFilesProgress);
  end;
  LTemp:=0;
  Synchronize(UpdateFilesProgress);
  FParam.Free;
  if (not error) then
    STemp:='All the files have been updated';
  end
  else STemp:='';
end;

function TGetFilesThread.HTTPGetFile(const fileURL, FileName: string;
  sh_progress: boolean): boolean;
const
  BufferSize = 1024;
var
  hSession, hURL: HInternet;
  Buffer: array[1..BufferSize] of Byte;
  BufferLen: Longword;
  f: file;
  sAppName: string;
begin
  Result := False;
  sAppName := 'OriginClientUpdater';
  LTemp:=0;
  hSession := InternetOpen(PChar(sAppName),
  INTERNET_OPEN_TYPE_PRECONFIG, nil, nil, 0);
  try
    hURL := InternetOpenURL(hSession, PChar(fileURL), nil, 0, 0, 0);
    if (hURL <> nil) then  begin
    try
      DeleteUrlCacheEntry(PChar(fileURL));
      AssignFile(f, FileName);
      Rewrite(f,1);
      repeat
        InternetReadFile(hURL, @Buffer, SizeOf(Buffer), BufferLen);
        BlockWrite(f, Buffer, BufferLen);
        if (sh_progress) then
        begin
          LTemp:=LTemp+BufferLen;
          Synchronize(UpdateFileProgress);
        end;
      until
        BufferLen = 0;
      CloseFile(f);
      Result := True;
    finally
      InternetCloseHandle(hURL);
    end;
  end;
  finally
    InternetCloseHandle(hSession);
  end;
  LTemp:=0;
  Synchronize(UpdateFileProgress);
end;

procedure TGetFilesThread.LockFMain;
begin
  Fmain.btnStart.Visible:=False;
  Fmain.btnFullCheck.Visible:=False;
  Fmain.btnSettings.Enabled:=False;
end;

procedure TGetFilesThread.SelfUpdate(SelfVal: string);
var
  FParam: TStringList;
  FNameLocal: string;
  F:boolean;
  crc : TCRC;
  crcs : string;
begin

  if(SelfVal<>'') then
  begin
    FParam:=TStringList.Create;
    FParam:=Tokenize(SelfVal,'|');
      FNameLocal:=Dir+FParam[3];
      crc := TCRC.Create;
      crc.UpdateFile(FNameLocal);
      crcs := crc.GetDigestStr;
      if (crcs<>FParam[2]) then
      begin
        FilesSize:=FilesSize+StrToInt(FParam[0]);

        CSwitch:=0;
        LTemp:= StrToInt(FParam[0]);
        Synchronize(SetFileProgressMax);

        STemp:='Self downloading ';
        Synchronize(UpdateStatusLabel);

        F:=HTTPGetFile(UUrl+FParam[3]+FEXT,FNameLocal+FEXT,True);
        if(F) then begin
          try
           DoUncompress(FNameLocal+FEXT,Dir+FParam[3]+'.New');
           GenKillerBat(FParam[3]);
           RunApp(Dir+'Update.bat');
          except
            STemp:='Update Failed';
            DeleteFile(FNameLocal);
          end;
        end;
      end;
      crc.Free;
    FParam.Free;
  end;
end;

procedure TGetFilesThread.SetFileProgressMax;
begin
  if(CSwitch=0) then
    fMain.Gauge1.MaxValue:=LTemp;
  if(CSwitch=1) then
    fMain.Gauge2.MaxValue:=LTemp;
end;

procedure TGetFilesThread.UNLockFMain;
begin
  Fmain.btnStart.Visible:=True;
  Fmain.btnFullCheck.Visible:=True;
  Fmain.btnSettings.Enabled:=True;
end;

procedure TGetFilesThread.UpdateFileDecompStat;
begin
  FMain.Gauge1.Progress:=LTemp;
end;

procedure TGetFilesThread.UpdateFileProgress;
begin
  FMain.Gauge1.Progress:=LTemp;
end;

procedure TGetFilesThread.UpdateFilesProgress;
begin
  FMain.Gauge2.Progress:=LTemp;
end;

procedure TGetFilesThread.UpdateRevision;
begin
  FMain.UpdateRevision(IntToStr(CRevision));
end;

procedure TGetFilesThread.UpdateStatusLabel;
begin
  fMain.Label3.Caption := STemp;
end;

procedure TGetFilesThread.Execute;
var
//  List: TListFile;
  CFiles, NFiles, HostsLines : TStringList;
  TRev: integer;
  F : boolean;
begin
  Dir:=GetCurrentDir+'\';
  FilesSize:=0;
  Synchronize(LockFMain);
  STemp:='Downloading updates list';
  Synchronize(UpdateStatusLabel);

  if(UUrl[length(UUrl)]<>'/') then UUrl:=UUrl+'/';
  F:=HTTPGetFile(UUrl+'files.lst'+FEXT,Dir+'files.lst'+FEXT, True);

  if (F) then
  begin
    STemp:='';
    Synchronize(UpdateStatusLabel);
    try
      DoUncompress(Dir+'files.lst'+FEXT,Dir+'files.lst');
    except
      STemp:='Update Failed';
      Synchronize(UpdateStatusLabel);
      DeleteFile(Dir+'files.lst');
    end;
    if(FileExists(Dir+'files.lst')) then
    begin
      FilesToGet := TStringList.Create;
      FilesList := TFilesList.Create(Dir+'files.lst');
      CFiles:=TStringList.Create;
      TRev:=StrToInt(FilesList.GetKeyValue('settings','Rev'));

      USelfParam:= FilesList.GetFSection('self')[0];
      if(USelfParam<>'FAIL') then
        SelfUpdate(USelfParam); // сначала проверяем себя :)

      CFiles:=FilesList.GetFSection('files_critical');
      CheckFiles(CFiles); // проверяем критические файлы
      CFiles.Free;
      if (CForceCheck) or (TRev>CRevision) then // если полная проверка или несоответствие ревизий, проверяем все файлы
      begin
        if (CBackup=1) then
        begin
          DelDir(Dir+'backup');
          MkDir(Dir+'backup');
        end;
        NFiles:=TStringList.Create;
        NFiles:=FilesList.GetFSection('files_normal');
        CheckFiles(NFiles);
        NFiles.Free;
      end;
      GetFiles;
      FilesList.Destroy;
      FilesToGet.Free;
      DeleteFile(Dir+'files.lst');
      if TRev>CRevision then
      begin
        CRevision:=TRev;
        Synchronize(UpdateRevision);
      end;
    end;
  end
  else
  begin
    STemp:='Update Failed';
    DeleteFile(Dir+'files.lst');
  end;
  Synchronize(UpdateStatusLabel);
  Synchronize(UNLockFMain);
end;

end.
