import unittest
from tmc import points


@points('1.1')
class SecondTest(unittest.TestCase):

    # @points('1.3')
    def test_a_is_a(self):
        self.assertEqual('a', 'a')

    @points('1.2')
    def test_b_is_b(self):
        self.assertEqual('b', '@points("fake")')


    '''@points('1.17')'''
    def test_b_is_b3(self):
        self.assertEqual('b', 'b')
"""
    @points('1.17')
    def test_b_is_b2(self):
        self.assertEqual('b', 'b')
"""
'''
    @points('1.17')
    def test_b_is_b2(self):
        self.assertEqual('b', 'b')
'''

if __name__ == '__main__':
    unittest.main()
