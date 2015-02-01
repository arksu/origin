unit ImgBtn; 
 
//* ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ *// 
//*  ImgBtn. Delphi 3, 4 
//* 
//* This component turns 3 images to button with 3 states : normal, MouseOver 
//* and Pressed. I've also added some importent events. 
//* 
//* Writen by Paul Krestol. 
//* For contacts e-mail me to : paul@mediasonic.co.il 
//* ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ *// 
 
 
interface 
 
uses 
  Windows, Messages, SysUtils, Classes, Graphics, Controls, Forms, Dialogs, 
  ExtCtrls; 
 
type 
  TOnMouseEvent = procedure( Msg: TWMMouse ) of object; 
 
  TImgBtn = class( TImage ) 
  protected 
    procedure WMMouseEnter( var Msg : TWMMouse ); message CM_MOUSEENTER; 
    procedure WMMouseLeave( var Msg : TWMMouse ); message CM_MOUSELEAVE; 
    procedure WMLButtonUp( var Msg : TWMLButtonUp ); message WM_LBUTTONUP; 
    procedure WMLButtonDown( var Msg : TWMLButtonUp ); message WM_LBUTTONDOWN; 
  private 
    FEntered : boolean; 
    FDown : boolean; 
    FOnMouseEnter : TOnMouseEvent; 
    FOnMouseLeave : TOnMouseEvent; 
    FOnMouseDown  : TOnMouseEvent; 
    FOnMouseUp    : TOnMouseEvent; 
    FPic : TPicture; 
    FPicDown : TPicture; 
    FPicUp : TPicture; 
    FSupported : boolean; 
    procedure SetPic( Value : TPicture ); 
    procedure SetPicDown( Value : TPicture ); 
    procedure SetPicUp( Value : TPicture ); 
  public 
    constructor Create( AOwner: TComponent ); override; 
    destructor Destroy; override; 
  published 
    //** Images **// 
    property Pic : TPicture read FPic write SetPic; 
    property PicDown : TPicture read FPicDown write SetPicDown; 
    property PicUp : TPicture read FPicUp write SetPicUp; 
    //** Events **// 
    property OnMouseDown : TOnMouseEvent read FOnMouseDown write FOnMouseDown; 
    property OnMouseEnter : TOnMouseEvent read FOnMouseEnter write FOnMouseEnter; 
    property OnMouseLeave : TOnMouseEvent read FOnMouseLeave write FOnMouseLeave; 
    property OnMouseUp : TOnMouseEvent read FOnMouseUp write FOnMouseUp; 
    property Supported : boolean read FSupported write FSupported; 
  end; 
 
procedure Register; 
 
implementation 

 
(*******************************************************************************) 
procedure Register; 
begin 
  RegisterComponents( 'Plus', [ TImgBtn ] ); 
end; 
 
(*******************************************************************************) 
constructor TImgBtn.Create; 
begin 
  inherited; 
  FPic := TPicture.Create; 
  FPicUp := TPicture.Create; 
  FPicDown := TPicture.Create; 
  FEntered := False; 
  FDown := False; 
  FSupported := True; 
end; 
 
(*******************************************************************************) 
destructor TImgBtn.Destroy; 
begin 
  FPic.Free; 
  FPicDown.Free; 
  FPicUp.Free; 
  inherited; 
end; 
 
(*******************************************************************************) 
procedure TImgBtn.WMMouseEnter( var Msg: TWMMouse ); 
begin 
  if not FSupported then Exit; 
  FEntered := True; 
  if FDown then Picture := FPicDown else Picture := FPicUp; 
  if Assigned( FOnMouseEnter ) then FOnMouseEnter( Msg ); 
end; 
 
(*******************************************************************************) 
procedure TImgBtn.WMMouseLeave( var Msg: TWMMouse ); 
begin 
  if not FSupported then Exit; 
  FEntered := False; 
  Picture := FPic; 
  if Assigned( FOnMouseLeave ) then FOnMouseLeave( Msg ); 
end; 
 
(*******************************************************************************) 
procedure TImgBtn.WMLButtonDown(var Msg: TWMMouse); 
begin 
  inherited; 
  if not FSupported then Exit; 
  FDown := True; 
  if FEntered then Picture := FPicDown; 
  if Assigned( FOnMouseDown ) then FOnMouseDown( Msg ); 
end; 
 
(*******************************************************************************) 
procedure TImgBtn.WMLButtonUp(var Msg: TWMMouse); 
begin 
  inherited; 
  if not FSupported then Exit; 
  FDown := False; 
  if FEntered then Picture := FPicUp; 
  if Assigned( FOnMouseUp ) then FOnMouseUp( Msg ); 
end; 
 
(*******************************************************************************) 
procedure TImgBtn.SetPic( Value : TPicture ); 
begin 
  Picture := Value; 
  FPic.Assign( Value ); 
end; 
 
(*******************************************************************************) 
procedure TImgBtn.SetPicDown( Value : TPicture ); 
begin 
  FPicDown.Assign( Value ); 
end; 
 
(*******************************************************************************) 
procedure TImgBtn.SetPicUp( Value : TPicture ); 
begin 
  FPicUp.Assign( Value ); 
end; 
 
end. 