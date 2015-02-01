unit main;

interface

uses
  Windows, Messages, SysUtils, Variants, Classes, Graphics, Controls, Forms,
  Dialogs, ExtCtrls, CoreX;

type
  TForm1 = class(TForm)
    procedure FormCreate(Sender: TObject);
    procedure FormPaint(Sender: TObject);
    procedure FormMouseDown(Sender: TObject; Button: TMouseButton;
      Shift: TShiftState; X, Y: Integer);
    procedure FormMouseUp(Sender: TObject; Button: TMouseButton;
      Shift: TShiftState; X, Y: Integer);
    procedure FormMouseMove(Sender: TObject; Shift: TShiftState; X, Y: Integer);
  private
    { Private declarations }
  public
    procedure InitPoints();
    procedure DrawLine;

    procedure DragBegin(x, y : Integer);
    procedure DragUpdate(x, y : Integer);
    procedure DragEnd;
  end;

  TBezierPoint = class
    prev, next : TBezierPoint;
    t : Single;
    p : TVec2f;

    constructor Create(at : Single; ap : TVec2f);
    procedure LinkN(p : TBezierPoint);
  end;

var
  Form1: TForm1;

  p0, p1, p2, p3 : TVec2f;
  drag_index : Integer;
  line : TBezierPoint;
  calcs : Integer;

const
  MAX_SEGMENTS = 10;
  BUILD_SEGMENTS = 3;

implementation

{$R *.dfm}

function CalculateBezierPoint(t : Single; p0, p1, p2, p3 : TVec2f) : TVec2f;
var
  u, uu, tt, uuu, ttt : Single;
begin
  u := 1 - t;
  tt := t*t;
  uu := u*u;
  uuu := uu*u;
  ttt := tt*t;

  Result := p0 * uuu;
  Result := Result + p1 * (3 * uu * t);
  Result := Result + p2 * (3 * u * tt);
  Result := Result + p3 * ttt;
end;

procedure FreeLine;
var
  p : TBezierPoint;
begin
  p := line;
  while p <> nil do
  begin
    if p.next <> nil then
    begin
      p := p.next;
      p.prev.Free;
    end else begin
      p.Free;
      p := nil;
    end;
  end;

  line := nil;
end;

procedure InsertPoints(bp0 : TBezierPoint);
var
  bp1, bpn : TBezierPoint;
  t0, t1, tmid : Single;
  pmid, left, right : TVec2f;
  pdot : Single;
const
  MIN_SQR_DISTANCE = 0.01;
  THRESHOLD = -0.999;
begin
  bp1 := bp0.next;
  t0 := bp0.t;
  t1 := bp1.t;

  if (bp0.p - bp1.p).LengthQ < MIN_SQR_DISTANCE then
    Exit;

  tmid := (t0 + t1) / 2;
  pmid := CalculateBezierPoint(tmid, p0, p1, p2, p3);
  left := (bp0.p - pmid).Normal;
  right := (bp1.p - pmid).Normal;

  pdot := left.Dot(right);
  if pdot > THRESHOLD then
  begin
    inc(calcs);
    bpn := TBezierPoint.Create(tmid, pmid);
    bpn.LinkN(bp0);
    bp1.LinkN(bpn);

    InsertPoints(bp0);
    InsertPoints(bpn);
  end;

end;

procedure ReBuildLine();
var
  i : Integer;
  t : Single;
  pn, pp : TBezierPoint;
begin
  FreeLine;
  calcs := 0;

  line := TBezierPoint.Create(0, CalculateBezierPoint(0, p0, p1, p2, p3));
  pp := line;
  for i := 1 to BUILD_SEGMENTS do
  begin
    t := i / BUILD_SEGMENTS;

    pn := TBezierPoint.Create(t, CalculateBezierPoint(t, p0, p1, p2, p3));
    pn.LinkN(pp);

    InsertPoints(pp);

    pp := pn;
  end;

  Form1.Caption := 'calcs : '+inttostr(calcs);
end;

////////////////////////////////////////////////////////////////////////////////
procedure TForm1.DragBegin(x, y: Integer);
var
  min, c : TVec2f;
  len : Single;
begin
  min := p0;
  c := Vec2f(x,y);
  len := c.Dist(min);
  drag_index := 0;

  if c.Dist(p1) < len then
  begin
    min := p1;
    len := c.Dist(min);
    drag_index := 1;
  end;

  if c.Dist(p2) < len then
  begin
    min := p2;
    len := c.Dist(min);
    drag_index := 2;
  end;

  if c.Dist(p3) < len then
  begin
    min := p3;
    len := c.Dist(min);
    drag_index := 3;
  end;
end;

procedure TForm1.DragEnd;
begin
  drag_index := -1;
end;

procedure TForm1.DragUpdate(x, y: Integer);
begin
  case drag_index of
    0 : p0 := Vec2f(x, y);
    1 : p1 := Vec2f(x, y);
    2 : p2 := Vec2f(x, y);
    3 : p3 := Vec2f(x, y);
  end;

  Repaint;
end;

procedure TForm1.DrawLine;
var
  i : Integer;
  t : Single;
  q0, q1: TVec2f;

  x1, y1 : Integer;

  bp : TBezierPoint;
  segs : Integer;
const
  POINT_RADIUS = 3;
begin
  Canvas.Brush.Style := bsSolid;
  Canvas.Brush.Color := clRed;
  x1 := Round(p0.x);
  y1 := Round(p0.y);
  Canvas.Ellipse(x1-POINT_RADIUS, y1-POINT_RADIUS, x1+POINT_RADIUS, y1+POINT_RADIUS);

  Canvas.Brush.Color := clGreen;
  x1 := Round(p3.x);
  y1 := Round(p3.y);
  Canvas.Ellipse(x1-POINT_RADIUS, y1-POINT_RADIUS, x1+POINT_RADIUS, y1+POINT_RADIUS);

  //////////////////////////////////////////////////////////////////////////////
  Canvas.Pen.Color := clRed;
  Canvas.MoveTo(Round(p0.x), Round(p0.y));
  Canvas.LineTo(Round(p1.x), Round(p1.y));

  Canvas.Pen.Color := clGreen;
  Canvas.MoveTo(Round(p2.x), Round(p2.y));
  Canvas.LineTo(Round(p3.x), Round(p3.y));

  //////////////////////////////////////////////////////////////////////////////
  Canvas.Pen.Color := clBlack;

  // рисуем саму кривую
//  q0 := CalculateBezierPoint(0, p0, p1, p2, p3);
//  Canvas.MoveTo(Round(q0.x), Round(q0.y));
//  for i := 1 to MAX_SEGMENTS do
//  begin
//    t := i / MAX_SEGMENTS;
//    q1 := CalculateBezierPoint(t, p0, p1, p2, p3);
//    Canvas.LineTo(Round(q1.x), Round(q1.y));
//    q0 := q1;
//  end;

  ReBuildLine;

  Canvas.Pen.Color := clBlue;
  Canvas.MoveTo(Round(line.p.x), Round(line.p.y));
  bp := line.next;
  segs := 0;
  while bp <> nil do
  begin
    Canvas.LineTo(Round(bp.p.x), Round(bp.p.y));
    bp := bp.next;
    Inc(segs);
  end;
  Canvas.Brush.Style := bsClear;
  Canvas.TextOut(0,0, IntToStr(segs));
end;

procedure TForm1.FormCreate(Sender: TObject);
begin
  InitPoints;
end;

procedure TForm1.FormMouseDown(Sender: TObject; Button: TMouseButton;
  Shift: TShiftState; X, Y: Integer);
begin
  if Button = mbLeft then
  begin
    DragBegin(X, Y);
    DragUpdate(X, Y);
  end;
end;

procedure TForm1.FormMouseMove(Sender: TObject; Shift: TShiftState; X,
  Y: Integer);
begin
  if drag_index >= 0 then
    DragUpdate(X, Y);
end;

procedure TForm1.FormMouseUp(Sender: TObject; Button: TMouseButton;
  Shift: TShiftState; X, Y: Integer);
begin
  if drag_index >= 0 then
  begin
    DragUpdate(X, Y);
    DragEnd;
  end;
end;

procedure TForm1.FormPaint(Sender: TObject);
begin
  DrawLine;
end;

procedure TForm1.InitPoints;
begin
  p0 := Vec2f(100,100);
  p1 := Vec2f(100, 400);
  p2 := Vec2f(600, 100);
  p3 := Vec2f(300, 100);
  line := nil;

  drag_index := -1;

  Repaint;
end;

{ TBezierPoint }

constructor TBezierPoint.Create(at: Single; ap: TVec2f);
begin
  t := at;
  p := ap;
end;

procedure TBezierPoint.LinkN(p: TBezierPoint);
begin
  p.next := Self;
  prev := p;
end;

end.
