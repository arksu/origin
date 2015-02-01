object fSelectFiles: TfSelectFiles
  Left = 0
  Top = 0
  Caption = 'Select Files'
  ClientHeight = 454
  ClientWidth = 664
  Color = clBtnFace
  Font.Charset = DEFAULT_CHARSET
  Font.Color = clWindowText
  Font.Height = -11
  Font.Name = 'Tahoma'
  Font.Style = []
  OldCreateOrder = False
  Position = poOwnerFormCenter
  OnClose = FormClose
  OnShow = FormShow
  PixelsPerInch = 96
  TextHeight = 13
  object Label2: TLabel
    Left = 4
    Top = 6
    Width = 67
    Height = 13
    Caption = 'Normal Files : '
  end
  object Label3: TLabel
    Left = 350
    Top = 6
    Width = 66
    Height = 13
    Caption = 'Critical Files : '
  end
  object Label1: TLabel
    Left = 2
    Top = 404
    Width = 31
    Height = 13
    Caption = 'Label1'
  end
  object Label4: TLabel
    Left = 350
    Top = 404
    Width = 31
    Height = 13
    Caption = 'Label4'
  end
  object ListBox1: TListBox
    Left = 2
    Top = 25
    Width = 310
    Height = 377
    Align = alCustom
    BevelInner = bvLowered
    BiDiMode = bdLeftToRight
    Ctl3D = True
    ItemHeight = 13
    MultiSelect = True
    ParentBiDiMode = False
    ParentCtl3D = False
    TabOrder = 0
    OnKeyDown = ListBox1KeyDown
  end
  object ListBox2: TListBox
    Left = 349
    Top = 24
    Width = 310
    Height = 377
    Align = alCustom
    ItemHeight = 13
    MultiSelect = True
    TabOrder = 1
    OnKeyDown = ListBox2KeyDown
  end
  object Button2: TButton
    Left = 319
    Top = 90
    Width = 23
    Height = 23
    Caption = '>'
    TabOrder = 2
    OnClick = Button2Click
  end
  object Button1: TButton
    Left = 319
    Top = 114
    Width = 23
    Height = 23
    Caption = '<'
    TabOrder = 3
    OnClick = Button1Click
  end
  object btnOk: TButton
    Left = 506
    Top = 418
    Width = 75
    Height = 25
    Caption = 'Ok'
    TabOrder = 4
    OnClick = btnOkClick
  end
  object Button4: TButton
    Left = 584
    Top = 418
    Width = 75
    Height = 25
    Caption = 'Cancel'
    TabOrder = 5
    OnClick = Button4Click
  end
end
