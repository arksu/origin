unit uMain;

interface

uses
  Windows, Messages, SysUtils, Variants, Classes, Graphics, Controls, Forms,
  Dialogs, StdCtrls, FileCtrl, IniFiles, ShellAPI, Gauges;

type
  TfMain = class(TForm)
    Label1: TLabel;
    Edit1: TEdit;
    Button1: TButton;
    Label2: TLabel;
    Edit2: TEdit;
    Button2: TButton;
    Label4: TLabel;
    Edit3: TEdit;
    CheckBox1: TCheckBox;
    Label3: TLabel;
    Edit4: TEdit;
    Label5: TLabel;
    btnBuild: TButton;
    Gauge1: TGauge;
    lblStatus: TLabel;
    procedure Button1Click(Sender: TObject);
    procedure Button2Click(Sender: TObject);
    procedure FormCreate(Sender: TObject);
    procedure FormClose(Sender: TObject; var Action: TCloseAction);
    procedure Label5Click(Sender: TObject);
    procedure CheckBox1Click(Sender: TObject);
    procedure btnBuildClick(Sender: TObject);
  private
    { Private declarations }
  public
    procedure ReadSettings;
    procedure WriteSettings;
    procedure ReadCriticalFiles;
    procedure WriteCriticalFiles;
  end;

var
  fMain: TfMain;

  SettingsFile: TIniFile;
  Settings : TStrings;
  Files : TStringList;
  CriticalFiles : TStrings;

implementation

uses
  uSelectFiles, Misc;

{$R *.dfm}

procedure GetDirFiles(Dir, BaseDir: string);
var
  SearchRec: TSearchRec;
  RelName: String;
begin
  if Dir <> '' then
    if Dir[length(Dir)] <> '\' then
      Dir := Dir + '\';
   if FindFirst(Dir + '*.*', faAnyFile, SearchRec) = 0 then
    repeat
      Application.ProcessMessages;
      if (SearchRec.name = '.') or (SearchRec.name = '..') then
        continue;
      if (SearchRec.Attr and faDirectory) <> 0 then
        begin
          GetDirFiles(Dir+SearchRec.name, BaseDir);
        end
      else
        begin
        RelName:=ReplaceStr(Dir+SearchRec.name,BaseDir+'\','');
        if (RelName<>Settings[3]) then
        begin
          Files.Add( LowerCase(RelName) );
        end;
        end
    until
      FindNext(SearchRec) <> 0;
  FindClose(SearchRec);
end;


procedure TfMain.btnBuildClick(Sender: TObject);
var
  Rev : integer;
  s : string;
  idx, i : Integer;
begin
  if fMain.Edit1.Text = fMain.Edit2.Text then
  begin
    ShowMessage('ERROR: Source Dir = Output Dir !');
  end
  else if (fMain.Edit1.Text='') or (fMain.Edit2.Text='') then
  begin
    ShowMessage(' ... this is not funny ... ');
  end
  else
  begin
    fMain.Gauge1.Progress:=0;
    Files:=TStringList.Create;
    GetDirFiles(fMain.Edit1.Text,fMain.Edit1.Text);

    // удалим файл апдейтер из всех списков. он обрабатывается особо
    idx := Files.IndexOf(LowerCase(Settings[2]));
    if idx >= 0 then
      Files.Delete(idx);

    idx := CriticalFiles.IndexOf(LowerCase(Settings[2]));
    if idx >= 0 then
      CriticalFiles.Delete(idx);


    // все критические файлы должны присутствовать в исходном списке
    i := 0;
    while i < CriticalFiles.Count do
    begin
      idx := Files.IndexOf( LowerCase(CriticalFiles[i]) );
      if idx < 0 then
      begin
        CriticalFiles.Delete(i);
        Continue;
      end;
      i := i +1;
    end;

    fSelectFiles.ListBox1.Items:= Files;
    Files.Free;

    fSelectFiles.ListBox2.Clear;
    fSelectFiles.ListBox2.Items.Assign(CriticalFiles);

    // удаляем критические файлы из нормального списка
    for s in CriticalFiles do
    begin
      idx := fSelectFiles.ListBox1.Items.IndexOf(LowerCase(s));
      if idx >= 0 then
        fSelectFiles.ListBox1.Items.Delete(idx);
    end;

    fSelectFiles.Visible:=True;
  end;
end;

procedure TfMain.Button1Click(Sender: TObject);
var
  temp: string;
begin
  SelectDirectory('Select Source Dir','',temp);
  Edit1.Text:=temp;
  Edit1.Hint:=temp;
end;

procedure TfMain.Button2Click(Sender: TObject);
var  temp: string;
begin
  SelectDirectory('Select Output Dir','',temp);
  Edit2.Text:=temp;
  Edit2.Hint:=temp;
end;

procedure TfMain.CheckBox1Click(Sender: TObject);
begin
if CheckBox1.Checked = True then
  Settings[1]:='1'
else Settings[1]:='0';
end;

procedure TfMain.FormClose(Sender: TObject; var Action: TCloseAction);
begin
  Settings[0]:=Edit3.Text;
  Settings[2]:=Edit4.Text;
  Settings[3]:=Edit1.Text;
  Settings[4]:=Edit2.Text;

  WriteSettings;
  Settings.Free;
  CriticalFiles.Free;
end;

procedure TfMain.FormCreate(Sender: TObject);
begin
  Settings := TStringList.Create;
  ReadSettings;

  CriticalFiles := TStringList.Create;
  ReadCriticalFiles;

  CheckBox1.Checked:=Settings[1]='1';

  Edit3.Text := Settings[0];
  Edit4.Text := Settings[2];
  Edit1.Text := Settings[3];
  Edit2.Text := Settings[4];
end;

procedure TfMain.Label5Click(Sender: TObject);
begin
  ShellExecute(Application.Handle,nil,PChar('http://origin-world.com'), nil, nil,SW_SHOWNORMAL);
end;

procedure TfMain.ReadCriticalFiles;
var
 CurDir:string;
begin
  CurDir:=GetCurrentDir;
  if FileExists(CurDir + '\critical.txt') then
    CriticalFiles.LoadFromFile(CurDir + '\critical.txt');
end;

procedure TfMain.ReadSettings;
var
 CurDir:string;
begin
  CurDir:=GetCurrentDir;
  SettingsFile:= TIniFile.Create(CurDir+'\settings.ini');

  Settings.Add(SettingsFile.ReadString('main','revision','1'));                        // 0
  Settings.Add(SettingsFile.ReadString('main','revision_autoinc','1'));                // 1
  Settings.Add(SettingsFile.ReadString('main','CustomName','OriginUpdater.exe'));      // 2
  Settings.Add(SettingsFile.ReadString('main','src_dir',''));                          // 3
  Settings.Add(SettingsFile.ReadString('main','out_dir',''));                          // 4

  SettingsFile.Free;
end;

procedure TfMain.WriteCriticalFiles;
var
 CurDir:string;
begin
  CurDir:=GetCurrentDir;
  if FileExists(CurDir + '\critical.txt') then DeleteFile(CurDir + '\critical.txt');
  CriticalFiles.SaveToFile(CurDir + '\critical.txt');
end;

procedure TfMain.WriteSettings;
var
 CurDir:string;
begin
  CurDir:=GetCurrentDir;
  SettingsFile:= TIniFile.Create(CurDir+'\settings.ini');

  SettingsFile.WriteString('main','revision',Settings[0]);
  SettingsFile.WriteString('main','revision_autoinc',Settings[1]);
  SettingsFile.WriteString('main','CustomName',Settings[2]);
  SettingsFile.WriteString('main','src_dir',Settings[3]);
  SettingsFile.WriteString('main','out_dir',Settings[4]);

  SettingsFile.Free;
end;

end.
