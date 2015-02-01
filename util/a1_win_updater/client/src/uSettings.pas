unit uSettings;

interface

uses
  Windows, Messages, SysUtils, Variants, Classes, Graphics, Controls, Forms,
  Dialogs, StdCtrls, Buttons, ExtCtrls;

type
  TfSettings = class(TForm)
    Image1: TImage;
    SpeedButton2: TSpeedButton;
    SpeedButton1: TSpeedButton;
    procedure btnCancelClick(Sender: TObject);
    procedure btnApplyClick(Sender: TObject);
    procedure FormCreate(Sender: TObject);
    procedure FormMouseMove(Sender: TObject; Shift: TShiftState; X, Y: Integer);
    procedure FormMouseDown(Sender: TObject; Button: TMouseButton;
      Shift: TShiftState; X, Y: Integer);
    procedure FormMouseUp(Sender: TObject; Button: TMouseButton;
      Shift: TShiftState; X, Y: Integer);
  private
    { Private declarations }
  public
    Draging: Boolean;
    X0, Y0: integer;

  end;

var
  fSettings: TfSettings;

implementation

uses
  uMain;

{$R *.dfm}


procedure TfSettings.btnApplyClick(Sender: TObject);
begin
  fSettings.Hide;
  fMain.Enabled := True;

end;

procedure TfSettings.btnCancelClick(Sender: TObject);
begin
  fSettings.Hide;
  fMain.Enabled := True;

end;

procedure TfSettings.FormCreate(Sender: TObject);
var
  regn, tmpRegn, x, y: integer;
  nullClr: TColor;
begin
  fSettings.brush.bitmap:=image1.picture.bitmap;
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
  SetWindowRgn(fSettings.handle, regn, true);
end;

procedure TfSettings.FormMouseDown(Sender: TObject; Button: TMouseButton;
  Shift: TShiftState; X, Y: Integer);
begin
  Draging := true;
  x0 := x;
  y0 := y;
end;

procedure TfSettings.FormMouseMove(Sender: TObject; Shift: TShiftState; X,
  Y: Integer);
begin
  if Draging = true then
  begin
    fSettings.Left := fSettings.Left + X - X0;
    fSettings.top := fSettings.top + Y - Y0;
  end;
end;

procedure TfSettings.FormMouseUp(Sender: TObject; Button: TMouseButton;
  Shift: TShiftState; X, Y: Integer);
begin
  Draging := false;
end;

end.
