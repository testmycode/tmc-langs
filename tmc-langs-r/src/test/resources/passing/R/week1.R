##These exercises are taken from the course "Data-analyysi R-ohjelmistolla"
#(https://wiki.helsinki.fi/pages/viewpage.action?pageId=135074576).
##

##Exercise 1

a1 <- 4 + 10
b1 <- 5*a1
c1 <- b1^3
d1 <- exp(b1)
e1 <- d1^(1/10)
f1 <- sin(c1)
res1 <- (b1 + c1 + e1 + f1)

##Exercise 2

a2 <- 51%%7

##Excercise 3
a3 <- 51%%7 + 51%/%7

##Excercise 4
v4_1 <- c(20, 5, -2, 3, 47)
v4_2 <- seq(0, 100, 5)
v4_3 <- c(v4_1, v4_2)
v4_4 <- v4_3[(v4_3 > 3) & (v4_3 < 50)]
v4_5 <- v4_4[(v4_4 %% 10) == 0]

##Exercise 5
v5_1 <- rep(0, 1, 50)
v5_1[c(F, T)] <- 2

sum5_1 <- sum(v5_1)
v5_1
v5_2 <- v5_1
v5_2[c(T, F)] <- 1.2
sum5_2 <- sum(v5_2)

##Excerice 6

A <- matrix(c(3, 5, 6, 1/2, sqrt(5), 16, 0, 2, 0),nrow = 3, ncol = 3, byrow = TRUE)


##Excercise 7
B <- c(1, 1, 0)%*% solve(A)

##Excercise 8
I_3 <- diag(c(1, 1, 1))
A_8 <- A %*%I_3

##Excercise 9
is_eq_matrix <- t(A) == A
is_eq_matrix <- F

##Excercise 10
C_10 <- matrix(c(runif(20, min = 1, max=20)), ncol=4)
v10_1 <- C_10[C_10 < 5]
number10 <- length(v10_1)

D_10 <- C_10
D_10[D_10 < 5] <- 10

E_10 <- D_10
E_10 <- D_10[c(T, T, T, T, F),c(F,T,T, T)]


##Excercise 11
C_11 <- matrix(1:100, ncol=2)
C_11[c(F, T)] <- NA

##Excercise 12
C_12 <- C_11
C_12[is.na(C_12)] <- 0
