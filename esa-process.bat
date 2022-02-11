if not exist %2 mkdir %2

rem call ./esa.bat --preprocess %1 %2\preprocessed.xml.bz2 %2\titles.xml.bz2 --title-exclusion "^(?!category:)[^:]+:[^ ].+$"

rem call ./esa.bat --preprocess %1 %2\preprocessed.xml.bz2 %2\titles.xml.bz2 %2\id-titles.txt --title-exclusion "^(?!category:)[^: ]+:[^ ].+$" "^(ad )?[0-9]+s?( bc)?$" "^(january|february|march|april|may|june|july|august|september|october|november|december) [0-9]{1,2}( [0-9]{4})?$ "^category talk:.+$"

call ./esa.bat --write-id-titles %2\preprocessed.xml.bz2 %2\id-titles.txt

rem call ./esa.bat --count-links-and-terms %2\preprocessed.xml.bz2 %2\titles.xml.bz2 %2\annotated.xml.bz2 --min-in-links 5 --min-out-links 5 --min-terms 100 --id-titles %2\id-titles.txt

rem call ./esa.bat --repeat-content %2\terms.xml.bz2 %2\repeated.xml.bz2 --repeat-title 4 --repeat-link 2

rem call ./esa.bat --write-rare-words %2\annotated.xml.bz2 %2\rare-words.txt 3 --filter classic ascii lower singular --stop-words en-default --min-word-length 3

rem call .\esa.bat --index %2\annotated.xml.bz2 --threads 12 --batch-size 1000 --filter classic ascii lower singular stemmer --stemmer-depth 3 --stop-words en-default %2\rare-words.txt --min-word-length 3 --tfidf-mode bm25+ --bm25-delta 0.5

rem call ./esa.bat --article-stats %2\annotated.xml.bz2 --filter classic ascii lower singular stemmer --stemmer-depth 3 --stop-words en-default