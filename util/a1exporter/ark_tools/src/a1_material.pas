unit a1_material;

interface

uses
  CoreX, xmd, SysUtils, a1_java;

type
  TNodeMaterial = record
    ShaderName : string;
    URL       : string;
    Name      : string;
    ShadeType : (stLambert, stPhong, stBlinn);
    Params    : TMaterialParams;
    Defines   : array of string;
    SamplerName : array [TMaterialSampler] of string;
    Material  : TMaterial;
    Skin      : Boolean;
    FxSkin    : Boolean;
    procedure Save(const FileName: string);
  end;

  procedure ConvertMaterial(filename : string);

implementation

procedure ConvertMaterial(filename : string);
var
  xml : TXML;
  mat : TNodeMaterial;
  ms : TMaterialSampler;
  s : string;
begin
  xml := TXML.Load(filename);

  with mat do
  begin
    with Params do
    begin
      Mode       := rmOpaque;
      DepthWrite := True;
      AlphaTest  := 1;
      CullFace   := cfBack;
      BlendType  := btNormal;
      Diffuse    := Vec4f(1, 1, 1, 1);
      Emission   := Vec3f(0, 0, 0);
      Reflect    := 0.2;
      Specular   := Vec3f(1, 1, 1);
      Shininess  := 10;

      CastShadow := False;
      ReceiveShadow := false;
    end;

    for ms := Low(Params.Sampler) to High(Params.Sampler) do
    begin
      SamplerName[ms] := '';
      with Params.Sampler[ms] do
      begin
        RepeatUV := Vec2f(1, 1);
        OffsetUV := Vec2f(0, 0);
        RotateUV := 0;
      end;
    end;

    ShaderName := 'ashader';
    Skin := False;
    FxSkin := False;
  end;

  mat.Name := xml.Params['name'];

  if xml['shade_type'] <> nil then
  begin
      if xml['shade_type'].Params['val'] = 'phong' then
        mat.ShadeType := stPhong
      else
      if xml['shade_type'].Params['val'] = 'blinn' then
        mat.ShadeType := stBlinn
      else
      if xml['shade_type'].Params['val'] = 'lambert' then
        mat.ShadeType := stLambert
      else
        Error('unknown shade type ' + xml['shade_type'].Params['val']);
  end else
    mat.ShadeType := stPhong;

  //----------------------------------------------------------------------------
  if xml['cullface'] <> nil then
  begin
      if xml['cullface'].Params['val'] = 'back' then
        mat.Params.CullFace := cfBack
      else
      if xml['cullface'].Params['val'] = 'none' then
        mat.Params.CullFace := cfBack
      else
      if xml['cullface'].Params['val'] = 'front' then
        mat.Params.CullFace := cfBack
      else
        Error('unknown cullface type ' + xml['cullface'].Params['val']);
  end;

  //----------------------------------------------------------------------------
  if xml['shininess'] <> nil then
  begin
      mat.Params.Shininess := Conv(xml['shininess'].Params['val'], 0.0);
  end;

  //----------------------------------------------------------------------------
  if xml['bump'] <> nil then
  begin
      mat.SamplerName[msNormal] := xml['bump']['texture'].Params['file'];
  end;

  //----------------------------------------------------------------------------
  if xml['reflective'] <> nil then
  begin
    if xml['reflective'].Params['val'] <> '' then
      mat.Params.Reflect := Conv( xml['reflective'].Params['val'] , 0.2);
    if xml['reflective']['texture'] <> nil then
      mat.SamplerName[msReflect] := xml['reflective']['texture'].Params['file'];
  end;

  //----------------------------------------------------------------------------
  if xml['diffuse'] <> nil then
  begin
    if xml['diffuse']['texture'] <> nil then
      mat.SamplerName[msDiffuse] := xml['diffuse']['texture'].Params['file'];
    if xml['diffuse']['color'] <> nil then
      mat.Params.Diffuse := TVec4f(Pointer(@ParseFloat(xml['diffuse']['color'].Content)[0])^);
  end;

  //----------------------------------------------------------------------------
  if xml['specular'] <> nil then
  begin
    if xml['specular']['texture'] <> nil then
      mat.SamplerName[msSpecular] := xml['specular']['texture'].Params['file'];
    if xml['specular']['color'] <> nil then
      mat.Params.Specular := TVec3f(Pointer(@ParseFloat(xml['specular']['color'].Content)[0])^)
  end;

  //----------------------------------------------------------------------------
  if xml['ambient'] <> nil then
  begin
    if xml['ambient']['texture'] <> nil then
      mat.SamplerName[msAmbient] := xml['ambient']['texture'].Params['file'];
  end;

  //----------------------------------------------------------------------------
  if xml['emission'] <> nil then
  begin
    if xml['emission']['texture'] <> nil then
      mat.SamplerName[msEmission] := xml['emission']['texture'].Params['file'];
    if xml['emission']['color'] <> nil then
      mat.Params.Emission := TVec3f(Pointer(@ParseFloat(xml['emission']['color'].Content)[0])^)
  end;

  //----------------------------------------------------------------------------
  if xml['transparent'] <> nil then
  begin
    if xml['transparent'].Params['val'] <> '' then
      mat.Params.Diffuse.w := mat.Params.Diffuse.w * Conv( xml['transparent'].Params['val'] , 0.0);
    if xml['transparent']['texture'] <> nil then
      mat.SamplerName[msMask] := xml['transparent']['texture'].Params['file'];
      if mat.SamplerName[msMask] <> '' then
        mat.Params.Mode := rmOpacity;
  end;

  //----------------------------------------------------------------------------
  if xml['cast_shadow'] <> nil then
  begin
      s := LowerCase(xml['cast_shadow'].Params['val']);
      mat.Params.CastShadow := (s = 'true') or (s = '1') or (s = 'yes');
  end;

  //----------------------------------------------------------------------------
  if xml['receive_shadow'] <> nil then
  begin
      s := LowerCase(xml['receive_shadow'].Params['val']);
      mat.Params.ReceiveShadow := (s = 'true') or (s = '1') or (s = 'yes');
  end;

  //----------------------------------------------------------------------------
  if xml['depth_write'] <> nil then
  begin
      s := LowerCase(xml['depth_write'].Params['val']);
      mat.Params.DepthWrite := (s = 'true') or (s = '1') or (s = 'yes');
  end;

  //----------------------------------------------------------------------------
  if xml['fx_skin'] <> nil then
  begin
      s := LowerCase(xml['fx_skin'].Params['val']);
      mat.FxSkin := (s = 'true') or (s = '1') or (s = 'yes');
  end;

  //----------------------------------------------------------------------------
  if xml['skin'] <> nil then
  begin
      s := LowerCase(xml['skin'].Params['val']);
      mat.Skin := (s = 'true') or (s = '1') or (s = 'yes');
  end;

  //----------------------------------------------------------------------------
  if xml['alpha_test'] <> nil then
  begin
      s := LowerCase(xml['alpha_test'].Params['val']);
      mat.Params.AlphaTest := StrToIntDef(s, 1);
  end;

  //----------------------------------------------------------------------------
  if xml['shader'] <> nil then
  begin
      s := LowerCase(xml['shader'].Params['val']);
      mat.ShaderName := s;
  end;

  xml.Free;

  mat.Save(OUT_DIR + mat.Name);
end;

{ TNodeMaterial }

procedure TNodeMaterial.Save(const FileName: string);
const
  SamplerDefine : array [TMaterialSampler] of string = (
    'MAP_DIFFUSE', 'MAP_NORMAL', 'MAP_SPECULAR', 'MAP_AMBIENT', 'MAP_EMISSION', 'MAP_ALPHAMASK', 'MAP_REFLECT', 'MAP_SHADOW',
    'MAP_MASK', 'MAP_MAP0', 'MAP_MAP1', 'MAP_MAP2', 'MAP_MAP3'
  );

  procedure AddDefine(const Define: string);
  var
    i, j : LongInt;
  begin
  // if not in array
    for i := 0 to Length(Defines) - 1 do
      if Defines[i] = Define then
        Exit;
  // insert
    for i := 0 to Length(Defines) - 1 do
      if Defines[i] > Define then
      begin
        SetLength(Defines, Length(Defines) + 1);
        for j := Length(Defines) - 1 downto i + 1 do
          Defines[j] := Defines[j - 1];
        Defines[i] := Define;
        Exit;
      end;
    SetLength(Defines, Length(Defines) + 1);
    Defines[Length(Defines) - 1] := Define;
  end;

var
  ms : TMaterialSampler;
  ss : TStream;
  i : LongInt;
  DCount : word;
  Samplers : TMaterialSamplers;
  sp : TSamplerParams;
begin
  Defines := nil;
// Set defines
  if Skin then
    AddDefine('SKIN');

  Samplers := [];
//  Writeln(Ord(ms));
  // 9 !!!!!!!!!!
  for ms := Low(ms) to High(ms) do
    if (SamplerName[ms] <> '') or ((ms = msShadow) and Params.ReceiveShadow) then
    begin
      AddDefine(SamplerDefine[ms]);
      Samplers := Samplers + [ms];
    end;
  Samplers := Samplers - [msShadow];

  if SamplerName[msReflect] <> '' then
    AddDefine('FX_REFLECT');

  if (SamplerName[msEmission] <> '') or (Params.Emission.LengthQ > EPS) then
    AddDefine('FX_EMISSION');

//  if ShadeType in [stPhong, stBlinn] then
    AddDefine('FX_SHADE');
//  AddDefine('FX_PLASTIC');

  case ShadeType of
    stPhong : AddDefine('FX_PHONG');
    stBlinn : AddDefine('FX_BLINN');
  end;

  if FxSkin then
    AddDefine('FX_SKIN');
//  AddDefine('FX_COLOR');

// Saving
  ss := TStream.Init(FileName + '.amt', True);
  if ss <> nil then
  begin
    // params
    ss.Write(Params.Mode, SizeOf(Params.Mode));
    TJavaStream.WriteBoolean(ss, Params.ReceiveShadow);
    TJavaStream.WriteBoolean(ss, Params.CastShadow);
    TJavaStream.WriteBoolean(ss, Params.DepthWrite);
    TJavaStream.WriteByte(ss, Params.AlphaTest);
    TJavaStream.WriteVec4f(ss, Params.Diffuse);
    TJavaStream.WriteVec3f(ss, Params.Emission);
    TJavaStream.WriteFloat(ss, Params.Reflect);
    TJavaStream.WriteVec3f(ss, Params.Specular);
    TJavaStream.WriteFloat(ss, Params.Shininess);
    ss.Write(Params.CullFace, SizeOf(Params.CullFace));
    ss.Write(Params.BlendType, SizeOf(Params.BlendType));

    for i := 0 to 8 do begin
      sp := Params.Sampler[ TMaterialSampler(i) ];
      TJavaStream.WriteVec2f(ss, sp.OffsetUV);
      TJavaStream.WriteVec2f(ss, sp.RepeatUV);
      TJavaStream.WriteFloat(ss, sp.RotateUV);
    end;
    //Stream.Write(Params, SizeOf(Params));

    ss.WriteAnsi(AnsiString(ShaderName));

    DCount := Length(Defines);
    ss.Write(DCount, SizeOf(DCount)); // Defines count
    for i := 0 to DCount - 1 do
      ss.WriteAnsi(AnsiString(Defines[i]));


//    ss.Write(Samplers, SizeOf(Samplers));
    for i := 0 to 8 do begin
      ms := TMaterialSampler(i);
      if ms in Samplers then
        ss.WriteAnsi(AnsiString(SamplerName[ms]))
      else
        ss.WriteAnsi('');
    end;

    ss.Free;
  end;

end;

end.
