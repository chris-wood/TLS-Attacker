# TLS-Attacker-Development

Examples:

TLS: 
- java -jar TLS-1.0-SNAPSHOT.jar client -connect localhost:51624 -cipher TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA -keystore ../resources/ec256.jks -password password -alias mykey

Attacks: 
- java -jar Attacks-1.0-SNAPSHOT-jar-with-dependencies.jar winshock -connect localhost:51624 -cipher TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA -keystore ../../resources/ec256.jks -password password -alias mykey -signature 0x820428032402403284024032 -signature_length 5000
- java -jar Attacks-1.0-SNAPSHOT-jar-with-dependencies.jar elliptic_test -connect localhost:54433 -named_curve SECP192R1 -public_point_base_x 0x9d42769dfdbe113a851bb6b01b1a515d893b5adbc1f61329 -public_point_base_y 0x74749ac0967a8ff4cc54d93187602dd67eb3d22970aca2ca -premaster_secret 0x9d42769dfdbe113a851bb6b01b1a515d893b5adbc1f61329 -cipher TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA
- java -jar Attacks-1.0-SNAPSHOT-jar-with-dependencies.jar bleichenbacher_test -connect localhost:51624

Fuzzer:
- java -jar Fuzzer-1.0-SNAPSHOT-jar-with-dependencies.jar simple_fuzzer -connect localhost:51626 -server_command_file ../resources/command-polarssl -max_fragment_length 4 -heartbeat_mode PEER_ALLOWED_TO_SEND -workflow_trace_type FULL -server_name test.de  -modified_variable_types TLS_CONSTANT,LENGTH,COUNT,PADDING,PUBLIC_KEY,CERTIFICATE -keystore ../resources/keystore-1024.jks -password password -max_transport_response_wait 300 -alias 1024_rsa