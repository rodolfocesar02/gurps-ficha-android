2026-06-19 06:47:18.425 13679-13755 MediaCodecList          com.gurps.ficha.visual               D  codecHandlesFormat: no format, so no extra checks
2026-06-19 06:47:18.425 13679-13755 MediaCodec              com.gurps.ficha.visual               I  Retry enabled for HDCP failure
2026-06-19 06:47:18.456 13679-13755 ApexCodecsLazy          com.gurps.ficha.visual               I  ApexCodecs loaded
2026-06-19 06:47:18.457 13679-13755 Codec2Client            com.gurps.ficha.visual               I  Available Codec2 services: "default" "software" "__ApexCodecs__"
2026-06-19 06:47:18.465 13679-13756 CCodec                  com.gurps.ficha.visual               D  allocate(c2.android.raw.decoder)
2026-06-19 06:47:18.469 13679-13756 CCodec                  com.gurps.ficha.visual               I  setting up 'default' as default (vendor) store
2026-06-19 06:47:18.483 13679-13756 CCodec                  com.gurps.ficha.visual               I  Created component [c2.android.raw.decoder] for [c2.android.raw.decoder]
2026-06-19 06:47:18.483 13679-13756 CCodecConfig            com.gurps.ficha.visual               D  read media type: audio/raw
2026-06-19 06:47:18.487 13679-13756 ReflectedParamUpdater   com.gurps.ficha.visual               D  extent() != 1 for single value type: algo.buffers.max-count.values
2026-06-19 06:47:18.487 13679-13756 ReflectedParamUpdater   com.gurps.ficha.visual               D  extent() != 1 for single value type: output.subscribed-indices.values
2026-06-19 06:47:18.488 13679-13756 ReflectedParamUpdater   com.gurps.ficha.visual               D  extent() != 1 for single value type: input.buffers.allocator-ids.values
2026-06-19 06:47:18.489 13679-13756 ReflectedParamUpdater   com.gurps.ficha.visual               D  extent() != 1 for single value type: output.buffers.allocator-ids.values
2026-06-19 06:47:18.490 13679-13756 ReflectedParamUpdater   com.gurps.ficha.visual               D  extent() != 1 for single value type: algo.buffers.allocator-ids.values
2026-06-19 06:47:18.490 13679-13756 ReflectedParamUpdater   com.gurps.ficha.visual               D  extent() != 1 for single value type: output.buffers.pool-ids.values
2026-06-19 06:47:18.491 13679-13756 ReflectedParamUpdater   com.gurps.ficha.visual               D  extent() != 1 for single value type: algo.buffers.pool-ids.values
2026-06-19 06:47:18.495 13679-13756 CCodecConfig            com.gurps.ficha.visual               I  query failed after returning 9 values (BAD_INDEX)
2026-06-19 06:47:18.495 13679-13756 CCodecConfig            com.gurps.ficha.visual               D  c2 config diff is Dict {
                                                                                                      c2::u32 coded.bitrate.value = 64000
                                                                                                      c2::u32 input.buffers.max-size.value = 65536
                                                                                                      c2::u32 input.delay.value = 0
                                                                                                      string input.media-type.value = "audio/raw"
                                                                                                      c2::u32 output.large-frame.max-size = 0
                                                                                                      c2::u32 output.large-frame.threshold-size = 0
                                                                                                      string output.media-type.value = "audio/raw"
                                                                                                      c2::u32 raw.channel-count.value = 2
                                                                                                      c2::u32 raw.pcm-encoding.value = 0
                                                                                                      c2::u32 raw.sample-rate.value = 44100
                                                                                                    }
2026-06-19 06:47:18.497 13679-13755 MediaCodec              com.gurps.ficha.visual               I  media_quality service unavailable, skipping updatePictureProfile
2026-06-19 06:47:18.497 13679-13756 CCodec                  com.gurps.ficha.visual               D  [c2.android.raw.decoder] buffers are bound to CCodec for this session
2026-06-19 06:47:18.497 13679-13756 CCodecConfig            com.gurps.ficha.visual               D  no c2 equivalents for durationUs
2026-06-19 06:47:18.497 13679-13756 CCodecConfig            com.gurps.ficha.visual               D  no c2 equivalents for track-id
2026-06-19 06:47:18.497 13679-13756 CCodecConfig            com.gurps.ficha.visual               D  no c2 equivalents for bits-per-sample
2026-06-19 06:47:18.497 13679-13756 CCodecConfig            com.gurps.ficha.visual               D  no c2 equivalents for flags
2026-06-19 06:47:18.499 13679-13756 CCodecConfig            com.gurps.ficha.visual               D  c2 config diff is   c2::u32 raw.channel-count.value = 1
2026-06-19 06:47:18.499 13679-13756 CCodec                  com.gurps.ficha.visual               D  encoding statistics level = 0
2026-06-19 06:47:18.499 13679-13756 CCodec                  com.gurps.ficha.visual               D  setup formats input: AMessage(what = 0x00000000) = {
                                                                                                      int32_t android._codec-pcm-encoding = 2
                                                                                                      int32_t bitrate = 64000
                                                                                                      int32_t channel-count = 1
                                                                                                      int32_t max-input-size = 65536
                                                                                                      string mime = "audio/raw"
                                                                                                      int32_t pcm-encoding = 2
                                                                                                      int32_t sample-rate = 44100
                                                                                                    }
2026-06-19 06:47:18.499 13679-13756 CCodec                  com.gurps.ficha.visual               D  setup formats output: AMessage(what = 0x00000000) = {
                                                                                                      int32_t android._codec-pcm-encoding = 2
                                                                                                      int32_t buffer-batch-max-output-size = 0
                                                                                                      int32_t buffer-batch-threshold-output-size = 0
                                                                                                      int32_t channel-count = 1
                                                                                                      string mime = "audio/raw"
                                                                                                      int32_t pcm-encoding = 2
                                                                                                      int32_t sample-rate = 44100
                                                                                                      int32_t channel-mask = 0
                                                                                                      int32_t android._config-pcm-encoding = 2
                                                                                                    }
2026-06-19 06:47:18.499 13679-13756 CCodecConfig            com.gurps.ficha.visual               I  query failed after returning 9 values (BAD_INDEX)
2026-06-19 06:47:18.503 13679-13756 C2Store                 com.gurps.ficha.visual               D  Using DMABUF Heaps
2026-06-19 06:47:18.504 13679-13756 CCodecBufferChannel     com.gurps.ficha.visual               D  [c2.android.raw.decoder#169] Created input block pool with allocatorID 16 => poolID 17 - OK (0)
2026-06-19 06:47:18.506 13679-13756 CCodecBufferChannel     com.gurps.ficha.visual               I  [c2.android.raw.decoder#169] Created output block pool with allocatorID 16 => poolID 52 - OK
2026-06-19 06:47:18.506 13679-13756 CCodecBufferChannel     com.gurps.ficha.visual               D  [c2.android.raw.decoder#169] Configured output block pool ids 52 => OK
2026-06-19 06:47:18.516 13679-13756 CCodecBuffers           com.gurps.ficha.visual               D  [c2.android.raw.decoder#169:1D-Output] received null buffer
2026-06-19 06:47:18.518 13679-13756 CCodecBufferChannel     com.gurps.ficha.visual               D  [c2.android.raw.decoder#169] MediaCodec discarded an unknown buffer
2026-06-19 06:47:18.518 13679-13756 CCodecBufferChannel     com.gurps.ficha.visual               D  [c2.android.raw.decoder#169] MediaCodec discarded an unknown buffer
2026-06-19 06:47:18.520 13679-13761 CCodec                  com.gurps.ficha.visual               D  hold CodecLooper(1) until release
2026-06-19 06:47:18.531 13679-13679 nativeloader            com.gurps.ficha.visual               D  Load /data/app/~~tSAPtBgXNcvxUz5taFcEYA==/com.gurps.ficha.visual-NXbAP28rjojzQ_azfZcJww==/split_config.x86_64.apk!/lib/x86_64/libfilament-jni.so using class loader ns clns-9 (caller=/data/app/~~tSAPtBgXNcvxUz5taFcEYA==/com.gurps.ficha.visual-NXbAP28rjojzQ_azfZcJww==/base.apk!classes3.dex): ok
2026-06-19 06:47:18.533 13679-13679 nativeloader            com.gurps.ficha.visual               D  Load /data/app/~~tSAPtBgXNcvxUz5taFcEYA==/com.gurps.ficha.visual-NXbAP28rjojzQ_azfZcJww==/split_config.x86_64.apk!/lib/x86_64/libgltfio-jni.so using class loader ns clns-9 (caller=/data/app/~~tSAPtBgXNcvxUz5taFcEYA==/com.gurps.ficha.visual-NXbAP28rjojzQ_azfZcJww==/base.apk!classes3.dex): ok
2026-06-19 06:47:18.535 13679-13679 nativeloader            com.gurps.ficha.visual               D  Load /data/app/~~tSAPtBgXNcvxUz5taFcEYA==/com.gurps.ficha.visual-NXbAP28rjojzQ_azfZcJww==/split_config.x86_64.apk!/lib/x86_64/libfilament-utils-jni.so using class loader ns clns-9 (caller=/data/app/~~tSAPtBgXNcvxUz5taFcEYA==/com.gurps.ficha.visual-NXbAP28rjojzQ_azfZcJww==/base.apk!classes3.dex): ok
2026-06-19 06:47:18.544 13679-13679 libc                    com.gurps.ficha.visual               W  Access denied finding property "vendor.mesa.virtgpu.kumquat"
2026-06-19 06:47:18.572 13679-13679 Filament                com.gurps.ficha.visual               I  FEngine (64 bits) created at 0x773bb3081320 (threading is enabled)
2026-06-19 06:47:18.573 13679-13767 Filament                com.gurps.ficha.visual               D  Using ASurfaceTexture
2026-06-19 06:47:18.576 13679-13767 Filament                com.gurps.ficha.visual               I  FEngine resolved backend: OpenGL
2026-06-19 06:47:18.579 13679-13767 libc                    com.gurps.ficha.visual               W  Access denied finding property "vendor.mesa.virtgpu.kumquat"
2026-06-19 06:47:18.599 13679-13767 Filament                com.gurps.ficha.visual               V  [Google (Intel)], [Android Emulator OpenGL ES Translator (Intel(R) Arc(TM) A750 Graphics)], [OpenGL ES 3.0 (4.5.0 - Build 32.0.101.8826)], [OpenGL ES GLSL ES 3.00]
2026-06-19 06:47:18.600 13679-13767 Filament                com.gurps.ficha.visual               V  Feature level: 1
                                                                                                    Active workarounds: 
                                                                                                    vao_doesnt_store_element_array_buffer_binding
2026-06-19 06:47:18.605 13679-13679 Filament                com.gurps.ficha.visual               I  Backend feature level: 1
2026-06-19 06:47:18.605 13679-13679 Filament                com.gurps.ficha.visual               I  FEngine feature level: 1
2026-06-19 06:47:18.770 13679-13679 GFXSTREAM               com.gurps.ficha.visual               E  [egl.cpp(2130)] eglQueryContext 32c0  EGL_BAD_ATTRIBUTE
2026-06-19 06:47:18.770 13679-13679 GFXSTREAM               com.gurps.ficha.visual               E  [egl.cpp(2131)] tid 13679: error 0x3004 (EGL_BAD_ATTRIBUTE)
2026-06-19 06:47:18.809 13679-13770 libc                    com.gurps.ficha.visual               W  Access denied finding property "vendor.mesa.virtgpu.kumquat"
2026-06-19 06:47:19.189 13679-13771 AudioSystem             com.gurps.ficha.visual               D  onNewServiceWithAdapter: media.audio_flinger service obtained 0x773c52ee7da0
2026-06-19 06:47:19.191 13679-13771 AudioSystem             com.gurps.ficha.visual               D  getService: IAudioFlingerService retrieved: 0x773c82ef7bc0  IAudioFlinger cached: 0x773c52ee7da0
2026-06-19 06:47:19.199   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:19.200   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:19.297   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:19.298   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:19.302   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:19.302   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:19.377   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:19.378   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:19.470   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:19.470   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:19.475   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:19.475   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:19.538   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:19.538   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:19.558   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:19.558   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:19.641   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:19.642   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:19.658   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:19.658   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:19.724   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:19.725   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:19.743   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:19.743   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:19.826   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:19.826   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:19.834   690-1915  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:19.834   690-1915  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:19.886   690-1915  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:19.887   690-1915  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:19.910   690-1915  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:19.910   690-1915  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:19.986   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:19.986   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:20.008   690-1915  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:20.008   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:20.074   690-1915  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:20.075   690-1915  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:20.127   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:20.127   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:20.162   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:20.162   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:20.227   690-1915  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:20.227   690-1915  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:20.286   690-1915  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:20.286   690-1915  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:20.326   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:20.327   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:20.386   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:20.387   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:20.426   690-1915  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:20.426   690-1915  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:20.498   690-1915  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:20.498   690-1915  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:20.526   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:20.526   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:20.594   690-1915  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:20.594   690-1915  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:20.625   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:20.625   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:20.706   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:20.706   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:20.727   690-1915  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:20.727   690-1915  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:20.771   690-1915  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:20.772   690-1915  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:20.826   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:20.827   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:20.898   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:20.898   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:20.910   690-1915  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:20.910   690-1915  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:20.986   690-1915  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:20.986   690-1915  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:20.990   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:20.991   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:21.089   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:21.089   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:21.091   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:21.091   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:21.166   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:21.166   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:21.175   690-1687  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:21.175   690-1687  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:21.246   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:21.247   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:21.258   690-1687  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:21.258   690-1687  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:21.323   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:21.323   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:21.341   690-1687  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:21.341   690-1687  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:21.414   690-1687  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:21.414   690-1687  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:21.426   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:21.427   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:21.498   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:21.498   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:21.526   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:21.526   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:21.598   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:21.598   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:21.672   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:21.673   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:21.709   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:21.710   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:21.772   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:21.773   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:21.822   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:21.822   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:21.889   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:21.889   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:21.950   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:21.950   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:21.988   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:21.988   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:22.056   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:22.056   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:22.105   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:22.105   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:22.134   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:22.134   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:22.205   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:22.205   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:22.262   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:22.263   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:22.325   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:22.325   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:22.358   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:22.359   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:22.424   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:22.424   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:22.483   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:22.483   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:22.540   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:22.541   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:22.598   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:22.598   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:22.640   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:22.641   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:22.710   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:22.711   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:22.756   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:22.756   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:22.794   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:22.794   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:22.856   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:22.856   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:22.902   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:22.903   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:22.973   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:22.973   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:23.046   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:23.046   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:23.073   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:23.073   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:23.161   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:23.161   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:23.189   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:23.189   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:23.238   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:23.239   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:23.288   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:23.289   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:23.346   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:23.346   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:23.408   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:23.408   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:23.446   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:23.446   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:23.505   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:23.505   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:23.513 13679-13759 AidlBufferPool          com.gurps.ficha.visual               D  bufferpool2 0x773bf2e4e098 : 0(0 size) total buffers - 0(0 size) used buffers - 0/5 (recycle/alloc) - 1/2 (fetch/transfer)
2026-06-19 06:47:23.513 13679-13759 AidlBufferPoolAcc       com.gurps.ficha.visual               D  evictor expired: 1, evicted: 1
2026-06-19 06:47:23.562   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:23.562   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:23.624   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:23.624   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:23.662   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:23.662   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:23.724   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:23.724   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:23.793   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:23.793   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:23.840   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:23.840   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:23.878   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:23.878   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:23.940   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:23.941   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:24.025   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:24.025   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:24.058   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:24.058   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:24.094   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:24.094   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:24.156   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:24.157   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:24.227   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:24.227   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:24.271   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:24.272   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:24.309   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:24.310   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:24.372   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:24.372   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:24.430   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:24.430   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:24.488   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:24.488   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:24.525   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:24.526   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:24.591   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:24.593   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:24.677   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:24.677   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:24.708   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:24.708   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:24.770   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:24.770   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:24.805   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:24.805   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:24.850   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:24.850   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:24.924   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:24.925   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:24.966   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:24.966   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:25.024   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:25.024   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:25.102   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:25.102   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:25.140   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:25.141   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:25.198   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:25.198   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:25.240   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:25.240   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:25.313   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:25.313   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:25.355   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:25.355   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:25.394   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:25.395   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:25.455   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:25.456   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:25.510   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:25.511   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:25.576   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:25.576   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:25.598   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:25.598   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:25.673   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:25.673   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:25.725   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:25.725   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:25.788   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:25.788   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:25.834   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:25.834   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:25.888   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:25.888   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:25.938   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:25.938   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:26.008   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:26.008   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:26.041   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:26.042   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:26.105   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:26.106   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:26.174   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:26.174   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:26.224   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:26.224   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:26.257   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:26.257   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:26.324   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:26.324   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:26.378   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:26.378   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:26.440   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:26.440   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:26.493   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:26.493   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:26.540   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:26.540   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:26.630   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:26.630   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:26.656   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:26.656   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:26.705   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:26.706   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:26.756   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:26.756   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:26.813   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:26.813   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:26.873   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:26.873   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:26.918   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:26.918   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:26.972   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:26.972   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:27.058   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:27.058   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:27.088   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:27.088   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:27.150   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:27.150   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:27.187   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:27.187   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:27.242   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:27.243   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:27.304   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:27.305   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:27.345   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:27.346   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:27.404   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:27.405   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:27.466   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:27.466   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:27.524   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:27.525   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:27.558   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:27.558   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:27.624   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:27.624   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:27.702   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:27.702   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:27.740   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:27.741   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:27.806   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:27.806   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:27.841   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:27.842   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:27.890   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:27.890   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:27.956   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:27.956   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:28.002   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:28.002   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:28.056   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:28.056   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:28.147   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:28.148   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:28.174   690-1687  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:28.175   690-1687  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:28.230   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:28.230   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:28.273   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:28.274   690-1915  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:28.330   690-1915  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:28.330   690-1915  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:28.387   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:28.387   690-1915  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:28.450   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:28.451   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:28.488   690-1915  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:28.489   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:28.554   690-1915  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:28.554   690-1915  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:28.605   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:28.605   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:28.638   690-1915  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:28.638   690-1915  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:28.704   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:28.705   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:28.781   690-1915  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:28.781   690-1915  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:28.824   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:28.824   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:28.866   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:28.866   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:28.924   690-1915  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:28.924   690-1915  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:28.986   690-1915  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:28.986   690-1915  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:29.040   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:29.040   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:29.073   690-1915  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:29.074   690-1915  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:29.140   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:29.141   690-1915  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:29.190   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:29.190   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:29.256   690-1915  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:29.257   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:29.311   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:29.311   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:29.356   690-1915  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:29.356   690-1915  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:29.429   690-1915  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:29.430   690-1915  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:29.471   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:29.471   690-1915  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:29.498   690-1915  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:29.498   690-1915  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:29.571   690-1915  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:29.572   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:29.626   690-1915  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:29.626   690-1915  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:29.646 13679-13679 Sceneview               com.gurps.ficha.visual               D  Engine destroyed
2026-06-19 06:47:29.649   690-1260  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-19 06:47:29.649   690-1242  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
