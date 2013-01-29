#ifndef __UTIL_H__
#define __UTIL_H__

#include <string>
#include <vector>

// Remove initial and trailing whitespace from string. Modifies string in-place
inline void trim(std::string& s)
{
    const std::size_t si = s.find_first_not_of(" \t\r\n");
    if (si != std::string::npos)
    {
        const std::size_t ei = s.find_last_not_of(" \t\r\n");
        const std::size_t l = (ei == std::string::npos ? ei : ei - si + 1);
        s = s.substr(si, l);
    }
    else
    {
        s = "";
    }
}

inline std::vector<std::string>
split(const std::string& s, const std::string& delim = " \t")
{
    typedef std::string::size_type size_type;
    std::vector<std::string> tokens;

    const size_type n = s.size();
    size_type i = 0;
    size_type e = 0;
    while (i < n && e < n)
    {
        e = s.find_first_of(delim, i); // Find end of current word
        if (e == std::string::npos)
        {   // Found last word
            tokens.push_back(s.substr(i, n - i));
        }
        else
        {
            if (i != e)
            {
                tokens.push_back(s.substr(i, e - i));
            }
            i = s.find_first_not_of(delim, e); // Find start of next word
        }
    }
    return tokens;
}

#endif
