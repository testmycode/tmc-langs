import unittest
from tmc import points


class FailingTest(unittest.TestCase):

    @points('1.1')
    def test_new(self):
        self.assertEqual("a", "b")

    @points('1.2')
    def test_two(self):
        self.failIf(True, "No false is not true at beginning\n   newlines  are kept")

if __name__ == '__main__':
    unittest.main()
