import unittest
import tmc
from tmc import points


@points('1.1')
class TestCase(unittest.TestCase):

    @points('1.2')
    def test_new(self):
        while True:
            continue

if __name__ == '__main__':
    unittest.main()
