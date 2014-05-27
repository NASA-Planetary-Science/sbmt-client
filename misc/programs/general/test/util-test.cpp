#include "gtest/gtest.h"
#include "util.h"

using namespace std;

TEST(StringUtilTest, Split) {
    string s = "dfsf dfd    dff  f";
    vector<string> splitString = split(s);
    EXPECT_EQ(4, splitString.size());
}

TEST(StringUtilTest, Trim) {
    string s = "    d     ";
    trim(s);
    EXPECT_EQ(string("d"), s);
}
