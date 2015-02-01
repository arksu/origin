object MainForm: TMainForm
  Left = 0
  Top = 0
  Caption = 'Convert helper'
  ClientHeight = 352
  ClientWidth = 673
  Color = clBtnFace
  Font.Charset = DEFAULT_CHARSET
  Font.Color = clWindowText
  Font.Height = -11
  Font.Name = 'Tahoma'
  Font.Style = []
  OldCreateOrder = False
  Position = poScreenCenter
  OnCreate = FormCreate
  DesignSize = (
    673
    352)
  PixelsPerInch = 96
  TextHeight = 13
  object Label1: TLabel
    Left = 16
    Top = 16
    Width = 114
    Height = 13
    Caption = #1042#1099#1073#1077#1088#1080' '#1090#1080#1087' '#1086#1087#1077#1088#1072#1094#1080#1080':'
  end
  object Label2: TLabel
    Left = 16
    Top = 88
    Width = 132
    Height = 13
    Caption = #1042#1074#1077#1076#1080' '#1088#1072#1073#1086#1095#1077#1077' '#1080#1084#1103' '#1092#1072#1081#1083#1072
  end
  object Label3: TLabel
    Left = 16
    Top = 152
    Width = 189
    Height = 13
    Caption = #1042#1074#1077#1076#1080' '#1080#1084#1103' '#1087#1086#1076#1076#1080#1088#1077#1082#1090#1086#1088#1080#1080' '#1074' '#1082#1083#1080#1077#1085#1090#1077
  end
  object Label4: TLabel
    Left = 16
    Top = 216
    Width = 179
    Height = 13
    Caption = #1042#1074#1077#1076#1080' '#1080#1084#1103' '#1082#1072#1090#1072#1083#1086#1075#1072' '#1089' '#1080#1089#1093#1086#1076#1085#1080#1082#1072#1084#1080
  end
  object ComboBox1: TComboBox
    Left = 16
    Top = 35
    Width = 145
    Height = 21
    Style = csDropDownList
    ItemIndex = 1
    TabOrder = 0
    Text = #1040#1085#1080#1084#1072#1094#1080#1103
    Items.Strings = (
      #1052#1077#1096' + '#1089#1082#1077#1083#1077#1090
      #1040#1085#1080#1084#1072#1094#1080#1103
      #1052#1072#1090#1077#1088#1080#1072#1083)
  end
  object Edit1: TEdit
    Left = 16
    Top = 107
    Width = 145
    Height = 21
    TabOrder = 1
  end
  object Edit2: TEdit
    Left = 16
    Top = 171
    Width = 145
    Height = 21
    TabOrder = 2
  end
  object btnStart: TButton
    Left = 16
    Top = 312
    Width = 75
    Height = 25
    Anchors = [akLeft, akBottom]
    Caption = #1055#1091#1089#1082
    TabOrder = 3
    OnClick = btnStartClick
  end
  object btnStop: TButton
    Left = 144
    Top = 312
    Width = 75
    Height = 25
    Anchors = [akLeft, akBottom]
    Caption = #1057#1090#1086#1087
    TabOrder = 4
    OnClick = btnStopClick
  end
  object Log: TMemo
    Left = 216
    Top = 8
    Width = 449
    Height = 278
    Anchors = [akLeft, akTop, akRight, akBottom]
    ScrollBars = ssBoth
    TabOrder = 5
  end
  object Edit3: TEdit
    Left = 16
    Top = 235
    Width = 121
    Height = 21
    TabOrder = 6
    Text = 'media'
  end
  object Timer1: TTimer
    Enabled = False
    OnTimer = Timer1Timer
    Left = 328
    Top = 144
  end
end
