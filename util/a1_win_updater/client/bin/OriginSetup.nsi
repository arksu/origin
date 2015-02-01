
Name "Origin"
Caption "Origin Game installer"
Icon "${NSISDIR}\Contrib\Graphics\Icons\orange-install.ico"
OutFile "OriginSetup.exe"

InstallColors FF8080 000030
XPStyle on
CRCCheck on

SetCompressor /SOLID lzma
RequestExecutionLevel admin

InstallDir "D:\Games\OriginGame"

;LicenseText "License"
LicenseData "license.txt"

;--------------------------------

Page license
Page components
Page directory
Page instfiles

;--------------------------------

; The stuff to install
Section "Origin game client (required)"

  SectionIn RO
  
  ; Set output path to the installation directory.
  SetOutPath $INSTDIR
  
  ; Put file there
  File "a1updater.exe"
    
SectionEnd

; Optional section (can be disabled by the user)
Section "Start Menu Shortcuts"

  CreateDirectory "$SMPROGRAMS\Origin"
  CreateShortCut "$SMPROGRAMS\Origin\Play Origin.lnk" "$INSTDIR\a1updater.exe" "" "$INSTDIR\a1updater.exe" 0
  
SectionEnd

Section "Desktop Shortcut"

  CreateShortCut "$DESKTOP\Origin.lnk" "$INSTDIR\a1updater.exe" "" "$INSTDIR\a1updater.exe" 0
  
SectionEnd