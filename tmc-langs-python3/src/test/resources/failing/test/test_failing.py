import unittest
from tmc import points


class FailingTest(unittest.TestCase):

    @points('1.1')
    def test_new(self):
        self.assertEqual("a", "b")

if __name__ == '__main__':
    unittest.main()
