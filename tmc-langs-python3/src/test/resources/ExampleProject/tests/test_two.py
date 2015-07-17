import unittest
import tmc
from tmc import points


@points('2.1')
class SecondTest(unittest.TestCase):

    @points('2.2', '2.3')
    def test_passing(self):
        self.assertEqual("a", "a")

    @points('2.4')
    def test_failing(self):
        self.assertEqual("a", "b")

    @points('2.5', '2.6')
    def test_erroring(self):
        pass

if __name__ == '__main__':
    unittest.main()
