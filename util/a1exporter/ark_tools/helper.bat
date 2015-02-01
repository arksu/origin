
set file=%1
set dir=%2
set mode=%3
set out_dir=out
set a1client_dir=f:\docs\a1client\trunk\media
set work_dir=.

pause 0



if %mode% == 0 goto mesh_skel
if %mode% == 1 goto anim
if %mode% == 2 goto material

rem ------------------------------------------------------------------
:mesh_skel
a1convert.exe media/%file%.a1blend

del /q %a1client_dir%\%dir%\%file%.ams
del /q %a1client_dir%\%dir%\%file%.ask

copy /y %work_dir%\%out_dir%\%file%.ams %a1client_dir%\%dir%\%file%.ams
copy /y %work_dir%\%out_dir%\%file%.ask %a1client_dir%\%dir%\%file%.ask
goto exit

rem ------------------------------------------------------------------
:anim
a1convert.exe media/%file%.a1blend

del /q %a1client_dir%\%dir%\%file%.aan

copy /y %work_dir%\%out_dir%\%file%.aan %a1client_dir%\%dir%\%file%.aan

goto exit

rem ------------------------------------------------------------------
:material
a1convert.exe media/%file%.xml

del /q %a1client_dir%\%dir%\%file%.amt

copy /y %work_dir%\%out_dir%\%file%.amt %a1client_dir%\%dir%\%file%.amt
goto exit

rem ------------------------------------------------------------------
:exit
pause 0
