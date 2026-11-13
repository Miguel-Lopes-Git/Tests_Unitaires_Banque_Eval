C5 fix: 
La vérification de la différence entre le solde et le découvert max dans la fonction debiter de la class Compte 
vérifier si la somme était <= ce qui cause qu'on ne peut pas être exactement à la somme max du découvert
Corresion : regarder uniquement si c'est inférieur

C6 fix:
La vérification du solde à 0 avec equals() compare aussi l'échelle (0 et 0.00 ne sont pas pareil) donc il faut utiliser à la place compareTo() qui 
compare la valeur numérique (0 et 0.00 sont pareil)