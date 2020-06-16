using System;
using Xunit;
using TestMyCode.CSharp.API.Attributes;

namespace PolicySamplTests
{
    [Points("3")]
    public class ProgramTest
    {
        [Fact]
        [Points("3.1")]
        public void Test1()
        {
            Assert.True("abc".Equals("abc"));
        }
    }
}
