object fMain: TfMain
  Left = 0
  Top = 0
  Caption = 'Update builder for Origin v1.0'
  ClientHeight = 167
  ClientWidth = 508
  Color = clBtnFace
  Font.Charset = DEFAULT_CHARSET
  Font.Color = clWindowText
  Font.Height = -11
  Font.Name = 'Tahoma'
  Font.Style = []
  OldCreateOrder = False
  Position = poScreenCenter
  Visible = True
  OnClose = FormClose
  OnCreate = FormCreate
  DesignSize = (
    508
    167)
  PixelsPerInch = 96
  TextHeight = 13
  object Label1: TLabel
    Left = 4
    Top = 7
    Width = 58
    Height = 13
    Caption = 'Source dir : '
  end
  object Label2: TLabel
    Left = 5
    Top = 33
    Width = 59
    Height = 13
    Caption = 'Output dir : '
  end
  object Label4: TLabel
    Left = 4
    Top = 61
    Width = 55
    Height = 13
    Caption = '#Revision :'
  end
  object Label3: TLabel
    Left = 4
    Top = 88
    Width = 72
    Height = 13
    Caption = 'Custom name :'
  end
  object Label5: TLabel
    Left = 8
    Top = 146
    Width = 134
    Height = 13
    Cursor = crHandPoint
    Anchors = [akLeft, akBottom]
    Caption = 'http://www.origin-world.com'
    Font.Charset = DEFAULT_CHARSET
    Font.Color = clBlue
    Font.Height = -11
    Font.Name = 'MS Sans Serif'
    Font.Style = [fsUnderline]
    ParentFont = False
    OnClick = Label5Click
    ExplicitTop = 224
  end
  object Gauge1: TGauge
    Left = 8
    Top = 112
    Width = 491
    Height = 17
    Progress = 0
    Visible = False
  end
  object lblStatus: TLabel
    Left = 96
    Top = 135
    Width = 305
    Height = 13
    Alignment = taCenter
    AutoSize = False
    Font.Charset = DEFAULT_CHARSET
    Font.Color = clWindowText
    Font.Height = -11
    Font.Name = 'Tahoma'
    Font.Style = []
    ParentFont = False
    Transparent = True
  end
  object Edit1: TEdit
    Left = 82
    Top = 4
    Width = 393
    Height = 21
    Color = clBtnFace
    Ctl3D = True
    ParentCtl3D = False
    ParentShowHint = False
    ReadOnly = True
    ShowHint = True
    TabOrder = 0
  end
  object Button1: TButton
    Left = 476
    Top = 6
    Width = 23
    Height = 19
    Caption = '...'
    TabOrder = 4
    OnClick = Button1Click
  end
  object Edit2: TEdit
    Left = 82
    Top = 31
    Width = 393
    Height = 21
    Color = clBtnFace
    Ctl3D = True
    ParentCtl3D = False
    ParentShowHint = False
    ReadOnly = True
    ShowHint = True
    TabOrder = 1
  end
  object Button2: TButton
    Left = 476
    Top = 31
    Width = 23
    Height = 19
    Caption = '...'
    TabOrder = 5
    OnClick = Button2Click
  end
  object Edit3: TEdit
    Left = 82
    Top = 58
    Width = 79
    Height = 21
    Ctl3D = True
    ParentCtl3D = False
    TabOrder = 2
    Text = '0'
  end
  object CheckBox1: TCheckBox
    Left = 167
    Top = 60
    Width = 141
    Height = 17
    Caption = 'Revision autoincrement'
    Ctl3D = False
    ParentCtl3D = False
    TabOrder = 3
    OnClick = CheckBox1Click
  end
  object Edit4: TEdit
    Left = 82
    Top = 85
    Width = 226
    Height = 21
    Ctl3D = True
    ParentCtl3D = False
    TabOrder = 6
  end
  object btnBuild: TButton
    Left = 425
    Top = 136
    Width = 75
    Height = 24
    Anchors = [akLeft, akBottom]
    Caption = 'Build'
    TabOrder = 7
    OnClick = btnBuildClick
    ExplicitTop = 153
  end
end
