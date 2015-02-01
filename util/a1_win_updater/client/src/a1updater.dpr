program a1updater;

uses
  Forms,
  uMain in 'uMain.pas' {fMain},
  defines in 'defines.pas',
  uSettings in 'uSettings.pas' {fSettings},
  uGetFiles in 'uGetFiles.pas',
  md5 in '..\..\md5.pas',
  uFilesList in 'uFilesList.pas',
  my_crc32 in '..\..\my_crc32.pas',
  lzo in '..\..\lzo.pas';

{$R *.res}

begin
  Application.Initialize;
  Application.Title := APP_TITLE;
  Application.CreateForm(TfMain, fMain);
  Application.CreateForm(TfSettings, fSettings);
  Application.Run;
end.
