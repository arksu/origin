program UpdateBuild;

uses
  Forms,
  uMain in 'uMain.pas' {fMain},
  uSelectFiles in 'uSelectFiles.pas' {fSelectFiles},
  uWorkThread in 'uWorkThread.pas',
  my_crc32 in '..\..\my_crc32.pas',
  lzo in '..\..\lzo.pas',
  md5 in '..\..\md5.pas';

{$R *.res}

begin
  Application.Initialize;
  Application.MainFormOnTaskbar := True;
  Application.CreateForm(TfMain, fMain);
  Application.CreateForm(TfSelectFiles, fSelectFiles);
  Application.Run;
end.
