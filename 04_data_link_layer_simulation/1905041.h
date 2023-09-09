#include <iostream>
#include <vector>
#include <string>
// #include <windows.h>

#ifndef LINK_LAYER
#define LINK_LAYER

struct input_data
{
    std::string data;
    int m;
    double p;
    std::string g;
};

enum COLOR {
    GREEN,
    CYAN,
    RED
};

struct recieved_frame {
    std::vector<int>* data;
    std::vector<int>* flipped_bit_positions;
};

struct input_data*                  input();
std::vector<std::vector<int>*>*     mk_ascii_block(struct input_data*);
void                                show_ascii_block(std::vector<std::vector<int>*>*, bool);
std::vector<std::vector<int>*>*     add_hammingcode(std::vector<std::vector<int>*>*);
std::vector<int>*                   serialize_block(std::vector<std::vector<int>*>*);
void                                show_serialized_block(std::vector<int>*);
std::vector<int>*                   add_crc_checksum(std::vector<int>*);
void                                show_frame_colored(std::vector<int>*, std::vector<int>*, COLOR);
struct recieved_frame*              simulate(std::vector<int>*, double);
bool                                check_crc_checksum(std::vector<int>*);
std::vector<std::vector<int>*>*     deserialize_frame(std::vector<int>*);
void                                show_recieved_block(std::vector<int>*, std::vector<int>*);
std::vector<std::vector<int>*>*     apply_hammingcode(std::vector<std::vector<int>*>*);
std::string                         mk_string(std::vector<std::vector<int>*>*);
void                                run();
#endif