unit uSelectFiles;

interface

uses
  Windows, Messages, SysUtils, Variants, Classes, Graphics, Controls, Forms,
  Dialogs, StdCtrls;

type
  TfSelectFiles = class(TForm)
    Label2: TLabel;
    ListBox1: TListBox;
    ListBox2: TListBox;
    Label3: TLabel;
    Button2: TButton;
    Button1: TButton;
    btnOk: TButton;
    Button4: TButton;
    Label1: TLabel;
    Label4: TLabel;
    procedure Button2Click(Sender: TObject);
    procedure Button1Click(Sender: TObject);
    procedure Button4Click(Sender: TObject);
    procedure ListBox1KeyDown(Sender: TObject; var Key: Word;
      Shift: TShiftState);
    procedure ListBox2KeyDown(Sender: TObject; var Key: Word;
      Shift: TShiftState);
    procedure FormClose(Sender: TObject; var Action: TCloseAction);
    procedure FormShow(Sender: TObject);
    procedure btnOkClick(Sender: TObject);
  private
    { Private declarations }
  public
    procedure UpdateTotalLabels;
  end;

var
  fSelectFiles: TfSelectFiles;

implementation

uses
  uMain, uWorkThread;

{$R *.dfm}

procedure TfSelectFiles.Button1Click(Sender: TObject);
begin
 ListBox2.MoveSelection(ListBox1);
 ListBox2.DeleteSelected;
 UpdateTotalLabels;
end;

procedure TfSelectFiles.Button2Click(Sender: TObject);
begin
 ListBox1.MoveSelection(ListBox2);
 ListBox1.DeleteSelected;
 UpdateTotalLabels;
end;

procedure TfSelectFiles.btnOkClick(Sender: TObject);
var
  PThread : TWorkThread;
  Rev, i : integer;
  Slines : TStrings;
begin
  fMain.Gauge1.Visible:=True;
  fMain.Gauge1.MaxValue:=ListBox1.Count + ListBox2.Count;

    Rev := StrToInt(fMain.Edit3.Text);

    PThread := TWorkThread.Create(True);
    PThread.FreeOnTerminate:=True;
    PThread.SourceDir:=fMain.Edit1.Text;
    PThread.OutputDir:=fMain.Edit2.Text;
    PThread.RevisionNum:=Rev;
    PThread.UpdaterClienName:=Settings[2];


    PThread.NormalFiles:=ListBox1.Items;
    PThread.CriticalFiles:=ListBox2.Items;
    PThread.Resume;

    if Settings[1] = '1' then
    begin
      Settings[0] := IntToStr(Rev+1);
      fMain.Edit3.Text:=Settings[0];
    end;

    fSelectFiles.Close;
end;

procedure TfSelectFiles.Button4Click(Sender: TObject);
begin
  Close;
end;

procedure TfSelectFiles.FormClose(Sender: TObject; var Action: TCloseAction);
begin
  CriticalFiles.Assign(ListBox2.Items);
  fMain.WriteCriticalFiles;

  fMain.Enabled:=True;
  fMain.btnBuild.Enabled:=True;
end;

procedure TfSelectFiles.FormShow(Sender: TObject);
begin
  fMain.Enabled:=False;
  fMain.btnBuild.Enabled:=False;
  UpdateTotalLabels;
end;

procedure TfSelectFiles.ListBox1KeyDown(Sender: TObject; var Key: Word;
  Shift: TShiftState);
begin
 if (Key = VK_DELETE) then begin
    ListBox1.DeleteSelected;
    UpdateTotalLabels;
 end;
end;

procedure TfSelectFiles.ListBox2KeyDown(Sender: TObject; var Key: Word;
  Shift: TShiftState);
begin
 if (Key = VK_DELETE) then begin
    ListBox2.DeleteSelected;
    UpdateTotalLabels;
 end;
end;

procedure TfSelectFiles.UpdateTotalLabels;
begin
  Label1.Caption :=IntToStr(ListBox1.Items.Count);
  Label4.Caption :=IntToStr(ListBox2.Items.Count);
end;

end.
