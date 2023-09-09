#include <algorithm>
#include "1905041.h"

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
    }
    else
    {
        std::cout << "data block (ascii code of m characters per row):\n";
        for (auto v : *block)
        {
            for (auto b : *v)
            {
                std::cout << b;
            }
            std::cout << "\n";
        }
    }
    std::cout << "\n";
}

void run()
{
    struct input_data *d = input();
    std::cout << "\ndata string after padding: " << d->data << "\n\n";
    std::vector<std::vector<int> *> *block = mk_ascii_block(d);
    show_ascii_block(block, false);
    delete block;
    delete d;
}
