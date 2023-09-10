#include <algorithm>
#include <chrono>
#include <random>
#include <windows.h>
#include "1905041.h"

inline void print_vector(std::vector<int> *num)
{
    for (auto i : *num)
        std::cout << i;
    std::cout << "\n";
}

inline unsigned int mylog2(unsigned int v)
{
    unsigned int r = 0;
    while (v >>= 1)
    {
        r++;
    }
    return r;
}

unsigned int num_size(std::vector<int> *num)
{
    unsigned int c = 0;
    for (; c < num->size(); c++)
        if (num->at(c))
            break;

    return (num->size() - c);
}

unsigned int first_set_at(std::vector<int> *num)
{
    unsigned int c = 0;
    for (; c < num->size(); c++)
        if (num->at(c))
            break;
    return c;
}

std::vector<int> *get_crc_rem(std::vector<int> *dividend, std::string g)
{
    std::vector<int> *rem = new std::vector<int>(*dividend);
    std::vector<int> *divisor = new std::vector<int>();

    for (int i = 0; i < (int)g.length(); i++)
        divisor->push_back(g[i] - '0');

    for (int i = 0; i < (int)(dividend->size() - g.length()); i++)
        divisor->push_back(0);

    while (num_size(divisor) >= g.length())
    {
        for (int i = 0; i < (int)rem->size(); i++)
            rem->at(i) ^= divisor->at(i);

        int shift_amount = first_set_at(rem) - first_set_at(divisor);
        if (shift_amount < 0)
        {
            std::cout << "rem f: " << first_set_at(rem) << ", divisor f: "
                      << first_set_at(divisor) << ", rem, divisor: \n";
            print_vector(rem);
            print_vector(divisor);
        }
        for (int i = divisor->size() - 1; i >= (int)shift_amount; i--)
            divisor->at(i) = divisor->at(i - shift_amount);

        for (int i = 0; i < (int)shift_amount; i++)
            divisor->at(i) = 0;
    }
    std::reverse(rem->begin(), rem->end());
    for (int i = 0; i < (int)(divisor->size() - g.length() + 1); i++)
        rem->pop_back();
    std::reverse(rem->begin(), rem->end());
    delete divisor;
    return rem;
}

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

std::vector<int> *ascii_to_bin(char c)
{
    std::vector<int> *r = new std::vector<int>();
    int k = 1;
    while (k < 1 << 8)
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
        }
    }

    return t;
}

void show_ascii_block(std::vector<std::vector<int> *> *block, bool is_hammed)
{
    if (is_hammed)
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
        std::cout << "data block (ascii code of m characters per row):\n";
        for (auto v : *block)
        {
            print_vector(v);
        }
    }
    std::cout << "\n";
}

std::vector<std::vector<int> *> *add_hammingcode(std::vector<std::vector<int> *> *block)
{
    for (auto b : *block)
    {
        b = mk_empty_hammedblock(b);
        b = fill_hammingcode(b);
    }

    return block;
}

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

    std::cout << '\n';
}

std::vector<int> *add_crc_checksum(std::vector<int> *block, std::string g)
{
    std::vector<int> *dividend = new std::vector<int>(*block);
    for (int i = 0; i < (int)g.size() - 1; i++)
        dividend->push_back(0);

    while (!(dividend->at(0)))
    {
        dividend->erase(dividend->begin() + 0);
    }

    // rem = ( x^r * M(x) ) % g(x)
    std::vector<int> *checksum = get_crc_rem(dividend, g);

    // c(x) = M(x) * x^r + rem
    for (int i = 0; i < (int)checksum->size(); i++)
        block->push_back(checksum->at(i));

    delete dividend;
    return block;
}

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
    std::cout << "\n";
}

std::vector<int> *simulate(int frame_size, double p)
{
    // std::default_random_engine re(std::chrono::steady_clock::now().time_since_epoch().count());
    std::mt19937 mt(std::chrono::steady_clock::now().time_since_epoch().count());
    std::uniform_real_distribution<> gen(0, 1);
    std::vector<int> *t = new std::vector<int>();
    for (int i = 0; i < frame_size; i++)
    {
        if (gen(mt) < p)
        {
            t->push_back(i);
        }
    }
    return t;
}

void run()
{
    // step 1
    struct input_data *d = input();
    std::cout << "\ndata string after padding: " << d->data << "\n\n";
    // step 2
    std::vector<std::vector<int> *> *block = mk_ascii_block(d);
    show_ascii_block(block, false);
    // step 3
    add_hammingcode(block);
    show_ascii_block(block, true);
    // step 4
    std::vector<int> *serialized = serialize_block(block);
    show_serialized_block(serialized, 0);
    // step 5
    serialized = add_crc_checksum(serialized, d->g);
    show_serialized_block(serialized, d->g.length() - 1);
    // step 6
    std::vector<int> *toggled_list = simulate((int)serialized->size(), d->p);
    show_frame_colored(serialized, toggled_list);

    std::cout << "end\n";
    delete serialized;
    for (auto b : *block)
        delete b;
    delete block;
    delete d;
}
