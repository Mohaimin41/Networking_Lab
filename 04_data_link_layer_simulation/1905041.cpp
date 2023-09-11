#include <algorithm>
#include <chrono>
#include <iostream>
#include <random>
#include <windows.h>

#include "1905041.h"

inline void print_vector(std::vector<int> *num)
{
    for (auto i : *num)
        std::cout << i;
    std::cout << "\n";
}

// integer 2 based log
inline unsigned int mylog2(unsigned int v)
{
    unsigned int r = 0;
    while (v >>= 1)
    {
        r++;
    }
    return r;
}

// absolute size of number in bits
unsigned int num_size(std::vector<int> *num)
{
    unsigned int c = 0;
    for (; c < num->size(); c++)
        if (num->at(c))
            break;

    return (num->size() - c);
}

// position of leftmost 1 in number padded with 0
unsigned int first_set_at(std::vector<int> *num)
{
    unsigned int c = 0;
    for (; c < num->size(); c++)
        if (num->at(c))
            break;
    return c;
}

// divide(continuos xor subtraction) dividend by generator g
std::vector<int> *get_crc_rem(std::vector<int> *dividend, std::string g)
{
    std::vector<int> *rem = new std::vector<int>(*dividend);
    std::vector<int> *divisor = new std::vector<int>();
    // making divisor from g
    for (int i = 0; i < (int)g.length(); i++)
        divisor->push_back(g[i] - '0');

    for (int i = 0; i < (int)(dividend->size() - g.length()); i++)
        divisor->push_back(0);
    // division loop
    while (num_size(divisor) >= g.length())
    {
        for (int i = 0; i < (int)rem->size(); i++) // subtract
            rem->at(i) ^= divisor->at(i);
        // right shift divisor to shift_amount
        int shift_amount = first_set_at(rem) - first_set_at(divisor);

        for (int i = divisor->size() - 1; i >= (int)shift_amount; i--)
            divisor->at(i) = divisor->at(i - shift_amount);

        for (int i = 0; i < (int)shift_amount; i++)
            divisor->at(i) = 0;
    }
    // remove padding from in front of remainder
    std::reverse(rem->begin(), rem->end());
    for (int i = 0; i < (int)(divisor->size() - g.length() + 1); i++)
        rem->pop_back();
    std::reverse(rem->begin(), rem->end());
    delete divisor;
    return rem;
}

// insert hamming code positions in number
std::vector<int> *mk_empty_hammedblock(std::vector<int> *block)
{
    int new_size = block->size() + mylog2(block->size()) + 1;
    for (int i = 1; i <= new_size; i++)
    {
        if (!(i > 0 && (i & (i - 1)))) // a power of 2
            block->insert(block->begin() + i - 1, 0);
    }
    return block;
}

// fill up hamming code with even parity
std::vector<int> *fill_hammingcode(std::vector<int> *block)
{
    for (int i = 1; i <= (int)block->size(); i++)
    {
        if (!(i & (i - 1))) // i is power of 2
        {
            int counter = 0;
            for (int j = 1; j <= (int)block->size(); j++)
            {
                // if j-th bit(int at vector) set
                // and j is a position checked by i-th bit(int at vector)
                if (block->at(j - 1) && (j & (1 << mylog2(i))))
                    counter++;
            }
            block->at(i - 1) = (counter & 1) ? 1 : 0; // even parity maintained
        }
    }
    return block;
}

// check each parity bit and toggle errored bit
// corrects only if 1 bit error, i.e. flipped bit position < number size
std::vector<int> *correct_by_hammingcode(std::vector<int> *block)
{
    int errored_bit = 0;
    for (int i = 1; i <= (int)block->size(); i++)
    {
        if (!(i & (i - 1))) // i is power of 2
        {
            int counter = 0;
            for (int j = 1; j <= (int)block->size(); j++)
            {
                // if j-th bit(int at vector) set
                // and j is a position checked by i-th bit(int at vector)
                if (block->at(j - 1) && (j & (1 << mylog2(i))))
                    counter++;
            }
            if (counter & 1) // odd parity found for i-th position correcting bit
                errored_bit += i;
        }
    }
    if (errored_bit && errored_bit <= (int)block->size()) // toggle
        block->at(errored_bit - 1) = !(block->at(errored_bit - 1));
    // remove check bits
    std::vector<int> t;

    for (int i = 1; i <= (int)block->size(); i++)
    {
        if ((i & (i - 1))) // not a power of 2
        {
            t.push_back(block->at(i - 1));
        }
    }
    block->swap(t);
    return block;
}

// mk vector of int representing bits of ascii char
std::vector<int> *ascii_to_bin(char c)
{
    std::vector<int> *r = new std::vector<int>();
    int k = 1;
    while (k < 1 << BYTE_SIZE)
    {
        r->push_back((c & k) ? 1 : 0);
        k <<= 1;
    }
    std::reverse(r->begin(), r->end());
    return r;
}

struct input_data *input()
{
    struct input_data *res = new input_data();
    std::cout << "enter data string: ";
    std::getline(std::cin, res->data);
    std::cout << "enter number of data bytes in a row (m): ";
    std::cin >> res->m;
    std::cout << "enter probability (p): ";
    std::cin >> res->p;
    std::cout << "enter generator polynomial: ";
    std::cin >> res->g;

    int l = res->data.size();
    if (l % res->m)
    {
        int rem = (l / res->m + 1) * res->m - l;
        for (int i = 0; i < rem; i++)
            res->data.append("~");
    }
    return res;
}

// make ascii char block, block is a vector of vector
// each vector is one row of block
std::vector<std::vector<int> *> *mk_ascii_block(struct input_data *d)
{
    std::vector<std::vector<int> *> *t = new std::vector<std::vector<int> *>();

    for (int i = 0, idx = 0; i < (int)d->data.size(); idx++)
    {
        t->push_back(new std::vector<int>());
        for (int j = 0; j < d->m; j++, i++)
        {
            std::vector<int> *bin = ascii_to_bin(d->data[i]);
            for (int b : *bin)
                t->at(idx)->push_back(b);
            delete bin;
        }
    }

    return t;
}

// shows block, color if shown after hamming code added
void show_ascii_block(std::vector<std::vector<int> *> *block, step s)
{
    if (s == AFTER_HAMMING_ADD)
    {
        std::cout << "data block after adding check bits:\n";
        for (auto v : *block)
        {
            int i = 1;
            for (auto b : *v)
            {
                if (i && (i & (i - 1)))
                {
                    std::cout << b;
                }
                else
                {
                    HANDLE hConsole = GetStdHandle(STD_OUTPUT_HANDLE);
                    SetConsoleTextAttribute(hConsole, FOREGROUND_GREEN);
                    std::cout << b;
                    SetConsoleTextAttribute(hConsole, 7);
                }
                i++;
            }
            std::cout << "\n";
        }
    }
    else
    {
        if (s == AFTER_BLOCK_MAKING)
            std::cout << "data block (ascii code of m characters per row):\n";
        else if (s == AFTER_REMOVING_HAMMING)
            std::cout << "data block after removing check bits:\n";
        for (auto v : *block)
        {
            print_vector(v);
        }
    }
    std::cout << "\n";
}

// take and add hamming code to each row of block
std::vector<std::vector<int> *> *add_hammingcode(std::vector<std::vector<int> *> *block)
{
    for (auto b : *block)
    {
        b = mk_empty_hammedblock(b);
        b = fill_hammingcode(b);
    }

    return block;
}

// serialize block to single row
std::vector<int> *serialize_block(std::vector<std::vector<int> *> *block)
{
    std::vector<int> *t = new std::vector<int>();

    for (int c = 0; c < (int)block->at(0)->size(); c++)
    {
        for (int r = 0; r < (int)block->size(); r++)
        {
            t->push_back(block->at(r)->at(c));
        }
    }
    return t;
}

// shows a serialized frame, bits appended (from append_count) shown in cyan
void show_serialized_block(std::vector<int> *serialized, int append_count)
{
    if (append_count)
        std::cout << "data bits after appending CRC checksum(sent frame):\n";
    else
        std::cout << "data bits after column wise serialization:\n";
    for (int i = 0; i < (int)serialized->size() - append_count; i++)
        std::cout << serialized->at(i);

    HANDLE hConsole = GetStdHandle(STD_OUTPUT_HANDLE);
    SetConsoleTextAttribute(hConsole, FOREGROUND_BLUE | FOREGROUND_GREEN);

    for (int i = serialized->size() - append_count; i < (int)serialized->size(); i++)
        std::cout << serialized->at(i);

    SetConsoleTextAttribute(hConsole, 7);

    std::cout << "\n\n";
}

// appends crc checksum to serailized fram
std::vector<int> *add_crc_checksum(std::vector<int> *frame, std::string g)
{
    // multipy by generator's highest power
    std::vector<int> *dividend = new std::vector<int>(*frame);
    for (int i = 0; i < (int)g.size() - 1; i++)
        dividend->push_back(0);
    // remove leading 0
    while (!(dividend->at(0)))
    {
        dividend->erase(dividend->begin() + 0);
    }

    // rem = ( x^r * M(x) ) % g(x)
    std::vector<int> *rem = get_crc_rem(dividend, g);

    // c(x) = x^r * M(x) + rem
    for (int i = 0; i < (int)rem->size(); i++)
        frame->push_back(rem->at(i));
    delete rem;
    delete dividend;
    return frame; // c(x)
}

// shows bits in position from toggled_list in read
void show_frame_colored(std::vector<int> *frame, std::vector<int> *toggled)
{
    std::cout << "recieved frame:\n";
    for (int i = 0; i < (int)frame->size(); i++)
    {
        if (std::find(toggled->begin(), toggled->end(), i) != toggled->end())
        {
            HANDLE hConsole = GetStdHandle(STD_OUTPUT_HANDLE);
            SetConsoleTextAttribute(hConsole, FOREGROUND_RED);
            std::cout << frame->at(i);
            SetConsoleTextAttribute(hConsole, 7);
        }
        else
        {
            std::cout << frame->at(i);
        }
    }
    std::cout << "\n\n";
}

// flips bits if random number in [0,1) is less then error probability threshold
// returns indices in frame where bit was flipped
std::vector<int> *simulate(std::vector<int> *frame, double p)
{
    // std::default_random_engine re(std::chrono::steady_clock::now().time_since_epoch().count());
    std::mt19937 mt(std::chrono::steady_clock::now().time_since_epoch().count());
    std::uniform_real_distribution<> gen(0, 1);
    std::vector<int> *t = new std::vector<int>();
    for (int i = 0; i < (int)frame->size(); i++)
    {
        if (gen(mt) < p)
        {
            t->push_back(i);
            frame->at(i) = !(frame->at(i));
        }
    }
    return t;
}

// error free frame should have remainder of size 0
bool has_crc_error(std::vector<int> *frame, std::string g)
{
    std::vector<int> *dividend = new std::vector<int>(*frame);

    while (!(dividend->at(0)))
    {
        dividend->erase(dividend->begin() + 0);
    }

    std::vector<int> *rem = get_crc_rem(dividend, g);

    int r = num_size(rem);
    delete rem;
    delete dividend;

    if (r)
        return false;
    else
        return true;
}

// make block (vector of vector) after removing checksum
std::vector<std::vector<int> *> *deserialize_frame(std::vector<int> *frame, struct input_data *d)
{
    std::vector<std::vector<int> *> *t = new std::vector<std::vector<int> *>();
    for (int i = 0; i < (int)d->g.length() - 1; i++) // checksum removed
        frame->pop_back();
    // number of rows in a block
    int r = frame->size() / (BYTE_SIZE * d->m + mylog2(BYTE_SIZE * d->m) + 1);

    for (int i = 0; i < r; i++)
        t->push_back(new std::vector<int>());

    for (int i = 0; i < (int)frame->size(); i++)
        t->at(i % r)->push_back(frame->at(i));

    return t;
}

// shows block with flippedb bits in red
void show_recieved_block(std::vector<std::vector<int> *> *block, std::vector<int> *toggled_list)
{
    std::cout << "data block after removing CRC checksum bits:\n";
    int r = 1;
    for (auto b : *block)
    {
        int c = 1;
        for (auto v : *b)
        {
            // show in red if bit position in frame is in toggled bits list
            if (std::find(
                    toggled_list->begin(), toggled_list->end(),
                    ((c - 1) * block->size() + r - 1)) != toggled_list->end())
            {
                HANDLE hConsole = GetStdHandle(STD_OUTPUT_HANDLE);
                SetConsoleTextAttribute(hConsole, FOREGROUND_RED);
                std::cout << v;
                SetConsoleTextAttribute(hConsole, 7);
            }
            else
            {
                std::cout << v;
            }
            c++;
        }
        r++;
        std::cout << "\n";
    }
    std::cout << '\n';
}

std::vector<std::vector<int> *> *apply_hammingcode(std::vector<std::vector<int> *> *block)
{
    for (auto b : *block)
        b = correct_by_hammingcode(b);
    return block;
}

std::string mk_string(std::vector<std::vector<int> *> *block)
{
    std::string s;

    for (auto b : *block)
    {
        int total_char = b->size() / BYTE_SIZE;
        // loop through 8 bits(int in vector) and make a char
        for (int c = 0; c < total_char; c++)
        {
            unsigned char ch = 0;
            for (int i = 0; i < BYTE_SIZE; i++)
            {
                ch |= (b->at(c * BYTE_SIZE + i)) ? 1 : 0;
                if (i < BYTE_SIZE - 1)
                    ch <<= 1;
            }
            s.push_back(ch);
        }
        std::cout << "\n";
    }

    return s;
}

void run()
{
    // step 1
    struct input_data *d = input();
    std::cout << "\ndata string after padding: " << d->data << "\n\n";
    // step 2
    std::vector<std::vector<int> *> *block = mk_ascii_block(d);
    show_ascii_block(block, AFTER_BLOCK_MAKING);
    // step 3
    add_hammingcode(block);
    show_ascii_block(block, AFTER_HAMMING_ADD);
    // step 4
    std::vector<int> *serialized = serialize_block(block);
    show_serialized_block(serialized, 0);
    // step 5
    serialized = add_crc_checksum(serialized, d->g);
    show_serialized_block(serialized, d->g.length() - 1);
    // step 6
    std::vector<int> *toggled_list = simulate(serialized, d->p);
    show_frame_colored(serialized, toggled_list);
    // step 7
    std::cout << "result of CRC checksum matching: ";
    if (has_crc_error(serialized, d->g))
    {
        std::cout << "no error detected\n\n";
    }
    else
    {
        std::cout << "error detected\n\n";
    }
    // step 8
    for (auto b : *block)
        delete b;
    delete block; // previous one wiil be overwritten
    block = deserialize_frame(serialized, d);
    show_recieved_block(block, toggled_list);
    // step 9
    block = apply_hammingcode(block);
    show_ascii_block(block, AFTER_REMOVING_HAMMING);
    // step 10
    std::cout << "output frame: " << mk_string(block);

    // clean up
    delete toggled_list;
    delete serialized;
    for (auto b : *block)
        delete b;
    delete block;
    delete d;
}
