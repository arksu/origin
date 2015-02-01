unit helper_main;

interface

uses
  Windows, Messages, SysUtils, Variants, Classes, Graphics, Controls, Forms,
  Dialogs, StdCtrls, ExtCtrls, ShellAPI, Registry;

type
  TMainForm = class(TForm)
    Label1: TLabel;
    ComboBox1: TComboBox;
    Label2: TLabel;
    Edit1: TEdit;
    Label3: TLabel;
    Edit2: TEdit;
    btnStart: TButton;
    btnStop: TButton;
    Log: TMemo;
    Timer1: TTimer;
    Label4: TLabel;
    Edit3: TEdit;
    procedure btnStartClick(Sender: TObject);
    procedure btnStopClick(Sender: TObject);
    procedure Timer1Timer(Sender: TObject);
    procedure FormCreate(Sender: TObject);
  private
    { Private declarations }
  public
    { Public declarations }
  end;

const
  A1_EXT = '.a1blend';
  A1_MAT_EXT = '.xml';
  BAT_FILE = 'helper.bat';

var
  MainForm: TMainForm;
  bat_name : string;
  work_dir : string;
  work_file : string;
  fname : string;
  last_refresh_time : Integer;

implementation

{$R *.dfm}

procedure TMainForm.btnStartClick(Sender: TObject);
var
  reg : TRegistry;
begin
  case ComboBox1.ItemIndex of
    0: bat_name := '0';
    1: bat_name := '1';
    2: bat_name := '2';
  end;

  work_dir := Edit2.Text;
  work_file := Edit1.Text;

  reg := TRegistry.Create;
  reg.RootKey := HKEY_CURRENT_USER;
  reg.OpenKey('software\a1_helper', True);
  reg.WriteString('work_dir', Edit2.Text );
  reg.WriteString('work_file', Edit1.Text);
  reg.WriteString('media_dir', Edit3.Text);
  reg.WriteInteger('mode', ComboBox1.ItemIndex);
  reg.CloseKey;
  reg.Free;
  

  fname := IncludeTrailingBackslash(ExtractFilePath(ParamStr(0))) + Edit3.Text + PathDelim + work_file;
  if bat_name = '2' then fname := fname + A1_MAT_EXT
  else fname := fname + A1_EXT;
  last_refresh_time := 0;
  
  Timer1.Enabled := True;
end;

procedure TMainForm.btnStopClick(Sender: TObject);
begin
  Timer1.Enabled := False;
end;

procedure TMainForm.FormCreate(Sender: TObject);
var
  reg : TRegistry;
begin
  reg := TRegistry.Create;
  reg.RootKey := HKEY_CURRENT_USER;
  reg.OpenKey('software\a1_helper', True);
  Edit2.Text := reg.ReadString('work_dir');
  Edit1.Text := reg.ReadString('work_file');
  Edit3.Text := reg.ReadString('media_dir');
  ComboBox1.ItemIndex := reg.ReadInteger('mode');
  reg.CloseKey;
  reg.Free;
end;

procedure TMainForm.Timer1Timer(Sender: TObject);
var
  t, res : Integer;
begin
  t := FileAge(fname);
  if (t <> last_refresh_time) then
  begin
    res := ShellExecute(Application.Handle, 'open', PChar(BAT_FILE), PChar(work_file + ' ' + work_dir + ' ' + bat_name), '', SW_SHOW);
  
    Log.Lines.Add(TimeToStr(Time) + ' : ' + fname);
//    Log.Lines.Add('result: '+IntToStr(res));
//    Log.Lines.Add('');
    
    last_refresh_time := t;
  end;  
  
end;

end.
