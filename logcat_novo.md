2026-06-18 22:02:00.499 11389-11389 WindowOnBackDispatcher  com.gurps.ficha.visual               W  OnBackInvokedCallback is not enabled for the application.
                                                                                                    Set 'android:enableOnBackInvokedCallback="true"' in the application manifest.
2026-06-18 22:02:00.533 11389-11389 WindowOnBackDispatcher  com.gurps.ficha.visual               D  setTopOnBackInvokedCallback (unwrapped): android.view.ViewRootImpl$$ExternalSyntheticLambda14@6b4bacc
2026-06-18 22:02:00.611 11389-11389 InsetsController        com.gurps.ficha.visual               D  hide(ime())
2026-06-18 22:02:00.611 11389-11389 ImeTracker              com.gurps.ficha.visual               I  com.gurps.ficha.visual:2f6661fa: onCancelled at PHASE_CLIENT_ALREADY_HIDDEN
2026-06-18 22:02:02.983 11389-12018 MediaCodecList          com.gurps.ficha.visual               D  codecHandlesFormat: no format, so no extra checks
2026-06-18 22:02:02.983 11389-12018 MediaCodec              com.gurps.ficha.visual               I  Retry enabled for HDCP failure
2026-06-18 22:02:02.990 11389-12019 CCodec                  com.gurps.ficha.visual               D  allocate(c2.android.raw.decoder)
2026-06-18 22:02:02.994 11389-12019 CCodec                  com.gurps.ficha.visual               I  setting up 'default' as default (vendor) store
2026-06-18 22:02:02.997 11389-11400 AidlBufferPool          com.gurps.ficha.visual               D  bufferpool2 0x773bf2ef0588 : 0(0 size) total buffers - 0(0 size) used buffers - 0/5 (recycle/alloc) - 1/2 (fetch/transfer)
2026-06-18 22:02:02.997 11389-11400 AidlBufferPool          com.gurps.ficha.visual               D  Destruction - bufferpool2 0x773bf2ef0588 cached: 0/0M, 0/0% in use; allocs: 5, 0% recycled; transfers: 2, 50% unfetched
2026-06-18 22:02:02.999 11389-12019 CCodec                  com.gurps.ficha.visual               I  Created component [c2.android.raw.decoder] for [c2.android.raw.decoder]
2026-06-18 22:02:02.999 11389-12019 CCodecConfig            com.gurps.ficha.visual               D  read media type: audio/raw
2026-06-18 22:02:03.001 11389-12019 ReflectedParamUpdater   com.gurps.ficha.visual               D  extent() != 1 for single value type: algo.buffers.max-count.values
2026-06-18 22:02:03.003 11389-12019 ReflectedParamUpdater   com.gurps.ficha.visual               D  extent() != 1 for single value type: output.subscribed-indices.values
2026-06-18 22:02:03.003 11389-12019 ReflectedParamUpdater   com.gurps.ficha.visual               D  extent() != 1 for single value type: input.buffers.allocator-ids.values
2026-06-18 22:02:03.003 11389-12019 ReflectedParamUpdater   com.gurps.ficha.visual               D  extent() != 1 for single value type: output.buffers.allocator-ids.values
2026-06-18 22:02:03.004 11389-12019 ReflectedParamUpdater   com.gurps.ficha.visual               D  extent() != 1 for single value type: algo.buffers.allocator-ids.values
2026-06-18 22:02:03.004 11389-12019 ReflectedParamUpdater   com.gurps.ficha.visual               D  extent() != 1 for single value type: output.buffers.pool-ids.values
2026-06-18 22:02:03.005 11389-12019 ReflectedParamUpdater   com.gurps.ficha.visual               D  extent() != 1 for single value type: algo.buffers.pool-ids.values
2026-06-18 22:02:03.008 11389-12019 CCodecConfig            com.gurps.ficha.visual               I  query failed after returning 9 values (BAD_INDEX)
2026-06-18 22:02:03.008 11389-12019 CCodecConfig            com.gurps.ficha.visual               D  c2 config diff is Dict {
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
2026-06-18 22:02:03.009 11389-12018 MediaCodec              com.gurps.ficha.visual               I  media_quality service unavailable, skipping updatePictureProfile
2026-06-18 22:02:03.010 11389-12019 CCodec                  com.gurps.ficha.visual               D  [c2.android.raw.decoder] buffers are bound to CCodec for this session
2026-06-18 22:02:03.010 11389-12019 CCodecConfig            com.gurps.ficha.visual               D  no c2 equivalents for durationUs
2026-06-18 22:02:03.010 11389-12019 CCodecConfig            com.gurps.ficha.visual               D  no c2 equivalents for track-id
2026-06-18 22:02:03.010 11389-12019 CCodecConfig            com.gurps.ficha.visual               D  no c2 equivalents for bits-per-sample
2026-06-18 22:02:03.010 11389-12019 CCodecConfig            com.gurps.ficha.visual               D  no c2 equivalents for flags
2026-06-18 22:02:03.011 11389-12019 CCodecConfig            com.gurps.ficha.visual               D  c2 config diff is   c2::u32 raw.channel-count.value = 1
2026-06-18 22:02:03.012 11389-12019 CCodec                  com.gurps.ficha.visual               D  encoding statistics level = 0
2026-06-18 22:02:03.012 11389-12019 CCodec                  com.gurps.ficha.visual               D  setup formats input: AMessage(what = 0x00000000) = {
                                                                                                      int32_t android._codec-pcm-encoding = 2
                                                                                                      int32_t bitrate = 64000
                                                                                                      int32_t channel-count = 1
                                                                                                      int32_t max-input-size = 65536
                                                                                                      string mime = "audio/raw"
                                                                                                      int32_t pcm-encoding = 2
                                                                                                      int32_t sample-rate = 44100
                                                                                                    }
2026-06-18 22:02:03.012 11389-12019 CCodec                  com.gurps.ficha.visual               D  setup formats output: AMessage(what = 0x00000000) = {
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
2026-06-18 22:02:03.012 11389-12019 CCodecConfig            com.gurps.ficha.visual               I  query failed after returning 9 values (BAD_INDEX)
2026-06-18 22:02:03.015 11389-12019 CCodecBufferChannel     com.gurps.ficha.visual               D  [c2.android.raw.decoder#35] Created input block pool with allocatorID 16 => poolID 19 - OK (0)
2026-06-18 22:02:03.015 11389-11389 Filament                com.gurps.ficha.visual               I  FEngine (64 bits) created at 0x773bb30673b0 (threading is enabled)
2026-06-18 22:02:03.015 11389-12025 Filament                com.gurps.ficha.visual               D  Using ASurfaceTexture
2026-06-18 22:02:03.016 11389-12025 Filament                com.gurps.ficha.visual               I  FEngine resolved backend: OpenGL
2026-06-18 22:02:03.017 11389-12019 CCodecBufferChannel     com.gurps.ficha.visual               I  [c2.android.raw.decoder#35] Created output block pool with allocatorID 16 => poolID 46 - OK
2026-06-18 22:02:03.017 11389-12019 CCodecBufferChannel     com.gurps.ficha.visual               D  [c2.android.raw.decoder#35] Configured output block pool ids 46 => OK
2026-06-18 22:02:03.022 11389-12025 libc                    com.gurps.ficha.visual               W  Access denied finding property "vendor.mesa.virtgpu.kumquat"
2026-06-18 22:02:03.031 11389-12019 CCodecBuffers           com.gurps.ficha.visual               D  [c2.android.raw.decoder#35:1D-Output] received null buffer
2026-06-18 22:02:03.035 11389-12019 CCodecBufferChannel     com.gurps.ficha.visual               D  [c2.android.raw.decoder#35] MediaCodec discarded an unknown buffer
2026-06-18 22:02:03.035 11389-12019 CCodecBufferChannel     com.gurps.ficha.visual               D  [c2.android.raw.decoder#35] MediaCodec discarded an unknown buffer
2026-06-18 22:02:03.035 11389-12019 CCodecBufferChannel     com.gurps.ficha.visual               D  [c2.android.raw.decoder#35] MediaCodec discarded an unknown buffer
2026-06-18 22:02:03.038 11389-12026 CCodec                  com.gurps.ficha.visual               D  hold CodecLooper(1) until release
2026-06-18 22:02:03.042 11389-12025 Filament                com.gurps.ficha.visual               V  [Google (Intel)], [Android Emulator OpenGL ES Translator (Intel(R) Arc(TM) A750 Graphics)], [OpenGL ES 3.0 (4.5.0 - Build 32.0.101.8826)], [OpenGL ES GLSL ES 3.00]
2026-06-18 22:02:03.046 11389-12025 Filament                com.gurps.ficha.visual               V  Feature level: 1
                                                                                                    Active workarounds: 
                                                                                                    vao_doesnt_store_element_array_buffer_binding
2026-06-18 22:02:03.048 11389-11389 Filament                com.gurps.ficha.visual               I  Backend feature level: 1
2026-06-18 22:02:03.048 11389-11389 Filament                com.gurps.ficha.visual               I  FEngine feature level: 1
2026-06-18 22:02:03.112 11389-11389 WindowOnBackDispatcher  com.gurps.ficha.visual               D  setTopOnBackInvokedCallback (unwrapped): null
2026-06-18 22:02:03.167 11389-12029 libc                    com.gurps.ficha.visual               W  Access denied finding property "vendor.mesa.virtgpu.kumquat"
2026-06-18 22:02:03.168 11389-11389 InsetsController        com.gurps.ficha.visual               D  hide(ime())
2026-06-18 22:02:03.169 11389-11389 ImeTracker              com.gurps.ficha.visual               I  com.gurps.ficha.visual:9fd49c48: onCancelled at PHASE_CLIENT_ALREADY_HIDDEN
2026-06-18 22:02:03.661   690-706   AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:03.663   690-1854  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:03.678   690-706   AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:03.679   690-706   AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:03.684   690-706   AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:03.684   690-706   AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:03.699   690-1063  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:03.699   690-706   AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:03.707   690-1901  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:03.707   690-1901  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:03.714   690-1063  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:03.714   690-1063  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:03.729   690-1063  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:03.729   690-1063  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:03.745   690-1901  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:03.746   690-706   AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:03.749   690-1901  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:03.749   690-1901  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:03.757   690-706   AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:03.757   690-706   AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:03.810   690-1901  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:03.810   690-1901  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:03.825   690-706   AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:03.826   690-706   AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:03.833   690-706   AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:03.833   690-706   AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:03.853   690-706   AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:03.853   690-706   AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:03.874   690-706   AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:03.875   690-706   AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:03.878   690-706   AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:03.878   690-706   AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:03.885   690-706   AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:03.885   690-706   AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:03.906   690-706   AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:03.906   690-706   AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:03.910   690-706   AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:03.910   690-706   AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:03.957   690-706   AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:03.957   690-706   AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:03.973   690-1901  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:03.974   690-706   AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.016   690-1901  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.016   690-1901  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.024   690-706   AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.024   690-706   AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.040   690-1063  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.041   690-1063  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.057   690-1901  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.057   690-1901  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.058   690-1063  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.059   690-1063  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.078   690-706   AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.078   690-706   AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.094   690-1901  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.094   690-1901  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.114   690-1063  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.114   690-1901  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.119   690-706   AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.120   690-706   AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.125   690-706   AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.125   690-706   AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.127   690-1901  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.128   690-1901  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.138   690-1901  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.138   690-1901  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.157   690-706   AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.158   690-706   AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.194   690-1901  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.194   690-1901  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.196   690-706   AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.197   690-706   AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.199   690-706   AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.200   690-706   AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.201   690-1901  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.202   690-1901  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.227   690-706   AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.227   690-706   AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.227   690-1063  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.228   690-1063  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.229   690-1063  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.229   690-706   AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.231   690-1063  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.231   690-1063  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.250   690-1063  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.250   690-1063  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.251   690-1901  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.252   690-1063  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.257   690-1063  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.257   690-1063  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.258   690-706   AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.259   690-1854  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.260   690-706   AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.260   690-706   AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.261   690-1064  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.261   690-1854  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.262   690-1063  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.262   690-706   AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.263   690-1063  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.263   690-1064  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.281   690-706   AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.282   690-706   AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.285   690-706   AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.286   690-706   AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.326   690-706   AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.326   690-706   AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.327   690-1063  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.327   690-706   AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.350   690-706   AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.350   690-706   AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.351   690-1063  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.352   690-1063  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.357   690-1063  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.358   690-706   AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.359   690-706   AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.360   690-1063  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.362   690-706   AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.362   690-1063  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.363   690-706   AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.364   690-1063  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.378   690-706   AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.378   690-1064  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.378   690-706   AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.378   690-1064  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.379   690-706   AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.379   690-1063  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.379   690-1064  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.381   690-1064  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.402   690-706   AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.402   690-706   AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.403   690-1063  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.404   690-1063  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.414   690-1063  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.415   690-1063  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.416   690-706   AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.417   690-706   AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.434   690-706   AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.434   690-1064  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.434   690-706   AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.434   690-1064  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.435   690-706   AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.435   690-1063  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.436   690-706   AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.436   690-1063  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.437   690-1064  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.437   690-706   AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.439   690-706   AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.440   690-706   AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.445   690-706   AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.445   690-1064  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.445   690-706   AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.445   690-1064  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.448   690-1064  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.448   690-1064  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.449   690-1854  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.450   690-1063  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.450   690-1854  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.450   690-1064  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.452   690-1063  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.452   690-1063  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.464   690-1063  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.464   690-1063  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.465   690-1063  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.466   690-1854  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.477   690-1063  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.477   690-1063  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.479   690-1854  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.480   690-1063  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.494   690-1063  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.494   690-1854  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.495   690-1064  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.495   690-1063  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.496   690-1064  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.496   690-1063  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.497   690-1064  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.497   690-706   AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.521   690-1064  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.521   690-1064  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.523   690-706   AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.524   690-706   AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.530   690-706   AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.530   690-1854  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.532   690-1064  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.533   690-1901  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.547   690-1901  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.547   690-1064  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.547   690-1064  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.547   690-1901  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.548   690-1901  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.548   690-1901  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.549   690-1854  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.549   690-1901  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.551   690-1064  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.551   690-1064  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.554   690-706   AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.554   690-706   AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.562   690-706   AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.562   690-1064  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.562   690-706   AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.562   690-1064  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.563   690-706   AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.563   690-1901  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.564   690-939   AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.564   690-1854  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.614   690-1064  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.615   690-1064  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.616   690-706   AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.616   690-706   AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.629   690-706   AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.629   690-1064  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.629   690-706   AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.629   690-1064  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.631   690-706   AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.631   690-939   AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.631   690-1064  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.631   690-706   AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.646   690-706   AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.646   690-939   AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.646   690-939   AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.646   690-706   AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.648   690-706   AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.648   690-1064  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.648   690-939   AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.648   690-1064  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.661   690-1064  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.661   690-939   AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.661   690-1064  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.661   690-939   AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.662   690-1064  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.663   690-706   AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.663   690-1854  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.663   690-1064  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.678   690-1064  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.678   690-939   AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.679   690-1064  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.680   690-1064  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.681   690-939   AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.682   690-1064  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.683   690-1064  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.684   690-1064  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.702   690-939   AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.703   690-939   AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.704   690-1064  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.705   690-1064  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.728 11389-11389 Sceneview               com.gurps.ficha.visual               D  Engine destroyed
2026-06-18 22:02:04.729   690-1854  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.730   690-1064  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.731   690-1854  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.731   690-1854  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.732   690-1854  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.733   690-1854  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.735   690-1854  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.735   690-1854  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.736   690-1854  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.736   690-1854  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.737   690-1854  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.738   690-1854  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.738   690-1854  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.739   690-1854  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.740   690-1854  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.740   690-1854  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.741   690-1854  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.742   690-1854  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.743   690-1854  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
2026-06-18 22:02:04.743   690-1854  AppOps                  system_server                        E  attributionTag  not declared in manifest of com.gurps.ficha.visual
