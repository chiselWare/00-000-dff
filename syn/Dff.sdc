
    create_clock -period 5.0 -waveform {0 2.5} clock
    set_input_delay -clock clock 1.0 {reset}
    set_input_delay -clock clock 1.0 {io_d}
    set_input_delay -clock clock 1.0 {io_enable}
    set_output_delay -clock clock 1.0 {io_q}
  