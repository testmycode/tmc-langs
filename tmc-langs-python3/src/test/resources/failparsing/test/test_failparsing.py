import unittest
from tmc import points


class FailParsingTest(unittest.TestCase):

    @points('1.1')
    def test_new(self):
        self.assertFalse(True, "No false is not true at beginning\n   newlines  are:kept:not:split")

if __name__ == '__main__':
    unittest.main()
