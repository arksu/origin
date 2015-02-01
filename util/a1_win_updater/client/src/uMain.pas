unit uMain;

interface

uses
  Windows, Messages, SysUtils, Variants, Classes, Graphics, Controls, Forms,
  Dialogs, Gauges, Buttons, IniFiles, StdCtrls, OleCtrls, SHDocVw, ExtCtrls,
  Wininet, ImgBtn, ComCtrls, ShlObj, ComObj, ActiveX, jpeg;


type
  TfMain = class(TForm)
    Gauge1: TGauge;
    Gauge2: TGauge;
    Image1: TImage;
    Label1: TLabel;
    Label2: TLabel;
    Label3: TLabel;
    btnStart: TImgBtn;
    btnFullCheck: TImgBtn;
    ImgBtn3: TImgBtn;
    ImgBtn4: TImgBtn;
    Panel1: TPanel;
    WebBrowser1: TWebBrowser;
    Image2: TImage;
    Image3: TImage;
    Image4: TImage;
    btnSettings: TImgBtn;
    Timer1: TTimer;
    Label5: TLabel;
    procedure FormCreate(Sender: TObject);
    procedure FormMouseDown(Sender: TObject; Button: TMouseButton;
      Shift: TShiftState; X, Y: Integer);
    procedure FormMouseMove(Sender: TObject; Shift: TShiftState; X,
      Y: Integer);
    procedure FormMouseUp(Sender: TObject; Button: TMouseButton;
      Shift: TShiftState; X, Y: Integer);
    procedure ImgBtn4Click(Sender: TObject);
    procedure ImgBtn3Click(Sender: TObject);
    procedure btnFullCheckClick(Sender: TObject);
    procedure btnStartClick(Sender: TObject);
    procedure FormClose(Sender: TObject; var Action: TCloseAction);
    procedure btnSettingsClick(Sender: TObject);
    procedure WebBrowser1NavigateComplete2(Sender: TObject;
      const pDisp: IDispatch; var URL: OleVariant);
    procedure Timer1Timer(Sender: TObject);
    procedure UpdateRevision(Rev: string);
  private
    { Private declarations }
  public
  Draging: Boolean;
  X0, Y0: integer;
  end;

var
  fMain: TfMain;
  UFSettings : TStrings;
  
implementation

uses
  defines, Misc, uSettings, uGetFiles;
//uses Frm2, GetFilesThr, Misc;

{$R *.dfm}

procedure TfMain.UpdateRevision(Rev: string);
var
  Settings: TInifile;
begin
  Settings := TInifile.Create(UFSettings[U_CURRENTDIR]+SETTINGS_FILE);
  Settings.WriteString('main','AtRevision',Rev);
  Settings.Free;
end;


function LoadSettings(): bool;
var
  Settings: TInifile;
begin
  Result:=False;

  UFSettings := TStringlist.Create;
  UFSettings.Add(GetCurrentDir+'\');

//  if(FileExists(UFSettings[U_CURRENTDIR]+SETTINGS_FILE)) then
//  begin
    Settings := TInifile.Create(UFSettings[U_CURRENTDIR]+SETTINGS_FILE);
    UFSettings.Add(Settings.ReadString('main','NewsUrl',''));
    UFSettings.Add(Settings.ReadString('main','UpdatesUrl','http://upd.origin-world.com/res/upd/'));
    UFSettings.Add(Settings.ReadString('main','LinkName','Origin'));
    UFSettings.Add(Settings.ReadString('main','Installed','0'));
    UFSettings.Add(Settings.ReadString('main','CreateBackup','0'));
    UFSettings.Add(Settings.ReadString('main','AtRevision','0'));
    UFSettings.Add(Settings.ReadString('main','RunCustom','run.bat'));
    Settings.Free;
    Result:=True;
//  end else begin
//    Settings := TInifile.Create(UFSettings[U_CURRENTDIR]+SETTINGS_FILE);
//    Settings.WriteString('main', 'NewsUrl', '');
//    Settings.WriteString('main', 'UpdatesUrl', 'http://upd.origin-world.com/res/upd/');
//    Settings.WriteString('main', 'LinkName', 'Origin');
//    Settings.WriteString('main', 'Installed', '0');
//    Settings.WriteString('main', 'CreateBackup', '0');
//    Settings.WriteString('main', 'AtRevision', '0');
//    Settings.WriteString('main', 'RunCustom', 'run.bat');
//    Settings.Free;
//  end;
end;

// создает ярлык на себя на рабочем столе
procedure CreateDesktopIcon(ilname, WorkDir, desc : string);
var
  IObj : IUnknown;
  SLink : IShellLink;
  PFile : IPersistFile;
  desk : string;
  lnkpath : WideString;
begin
  if(ilname<>'') then begin
  SetLength(desk, MAX_PATH+1);
  SHGetSpecialFolderPath(0, PChar(desk),CSIDL_DESKTOPDIRECTORY,False);
  lnkpath:= PChar(desk)+'\'+ilname+'.lnk';
  IObj := CreateComObject(CLSID_ShellLink);
  SLink := IObj as IShellLink;
  PFile := IObj as IPersistFile;
  with SLink do
  begin
    SetDescription(PChar(desc));
    SetPath(PChar(Application.ExeName));
    SetWorkingDirectory(PChar(WorkDir));
  end;
  PFile.Save(PWChar(WideString(lnkpath)), FALSE);
  end;
end;




procedure TfMain.FormCreate(Sender: TObject);
var
  regn, tmpRegn, x, y: integer;
  nullClr: TColor;
  s_load: bool;
  Settings: TInifile;
begin
  s_load:=LoadSettings();
  if (s_load) then
  begin
    if (UFSettings[U_INSTALLED]='0') then
    begin
      Settings := TInifile.Create(UFSettings[U_CURRENTDIR]+SETTINGS_FILE);
      Settings.WriteString('main','Installed','1');
      Settings.Free;
      CreateDesktopIcon(UFSettings[U_LINK_NAME],UFSettings[U_CURRENTDIR],LNK_DESC);
    end;
  end
  else
  begin
    FMain.Timer1.Enabled:=False;
    ShowMessage('ERROR: file '+SETTINGS_FILE+' Not Found !');
    Application.Terminate; // .close здесь не пройдет 
  end;

  // Наводим красивость на форму ...
  FMain.brush.bitmap:=Image1.picture.bitmap;
  nullClr := image1.picture.Bitmap.Canvas.Pixels[0, 0];
  regn := CreateRectRgn(0, 0, image1.picture.Graphic.Width,
  image1.picture.Graphic.Height);
  for x := 1 to image1.picture.Graphic.Width do
    for y := 1 to image1.picture.Graphic.Height do
      if image1.picture.Bitmap.Canvas.Pixels[x - 1, y - 1] = nullClr then
      begin
        tmpRegn := CreateRectRgn(x - 1, y - 1, x, y);
        CombineRgn(regn, regn, tmpRegn, RGN_DIFF);
        DeleteObject(tmpRegn);
      end;
  SetWindowRgn(FMain.handle, regn, true);
end;

procedure TfMain.FormMouseDown(Sender: TObject; Button: TMouseButton;
  Shift: TShiftState; X, Y: Integer);
begin
  Draging := true;
  x0 := x;
  y0 := y;
end;

procedure TfMain.FormMouseUp(Sender: TObject; Button: TMouseButton;
  Shift: TShiftState; X, Y: Integer);
begin
  Draging := false;
end;

procedure TfMain.FormMouseMove(Sender: TObject; Shift: TShiftState; X,
  Y: Integer);
begin
  if Draging = true then
  begin
    FMain.Left := FMain.Left + X - X0;
    FMain.top := FMain.top + Y - Y0;
  end;
end;

procedure TfMain.ImgBtn4Click(Sender: TObject);
begin
  FMain.Close;
end;

procedure TfMain.ImgBtn3Click(Sender: TObject);
begin
  FMain.Close;
end;

procedure TfMain.btnFullCheckClick(Sender: TObject);
var
  WThread : TGetFilesThread;
begin
  Label3.Caption:='';
  FMain.Timer1.Enabled:=False;

  WThread:=TGetFilesThread.Create(True);
  WThread.FreeOnTerminate:=True;
  WThread.UpdatesUrl:=UFSettings[U_UPDATES_URL];
  WThread.ForceCheck:=True;
  WThread.CreateBackup:=StrToInt(UFSettings[U_CREATE_BACKUP]);
  WThread.LocalRevision:=StrToInt(UFSettings[U_REVISION]);
  WThread.Resume;
end;

procedure TfMain.btnStartClick(Sender: TObject);
begin
  RunApp(UFSettings[U_CURRENTDIR]+UFSettings[U_RUN]);
  FMain.Close;
end;

procedure TfMain.FormClose(Sender: TObject; var Action: TCloseAction);
begin
 UFSettings.Free;
end;

procedure TfMain.btnSettingsClick(Sender: TObject);
begin
 FMain.Enabled:=False;
 fSettings.Show;
end;

procedure TfMain.WebBrowser1NavigateComplete2(Sender: TObject;
  const pDisp: IDispatch; var URL: OleVariant);
begin
 FMain.Panel1.Visible:=True;
 FMain.Image2.Visible:=True;
 FMain.Image3.Visible:=True;
 FMain.Image4.Visible:=True;
end;

procedure TfMain.Timer1Timer(Sender: TObject);
var
  WThread : TGetFilesThread;
begin
  FMain.Timer1.Enabled:=False;
  if (UFSettings[U_NEWS_URL] <> '') then
    WebBrowser1.Navigate(UFSettings[U_NEWS_URL]);
  Label3.Caption:='';

  WThread:=TGetFilesThread.Create(True);
  WThread.FreeOnTerminate:=True;
  WThread.UpdatesUrl:=UFSettings[U_UPDATES_URL];
  WThread.ForceCheck:=False;
  WThread.CreateBackup:=StrToInt(UFSettings[U_CREATE_BACKUP]);
  WThread.LocalRevision:=StrToInt(UFSettings[U_REVISION]);
  WThread.Resume;
end;

end.
