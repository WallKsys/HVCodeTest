## API Endpoint

You can request the data using the following endpoint:

```
http://app-homevision-staging.herokuapp.com/api_project/houses
```

This route by itself will respond with a default list of houses (or a server error!). You can use the following URL parameters:

- `page`: the page number you want to retrieve (default is 1)
- `per_page`: the number of houses per page (default is 10)

## Requirements

- Requests the first 10 pages of results from the API
- Parses the JSON returned by the API
- Downloads the photo for each house and saves it in a file with the name formatted as:
    
    `[id]-[address].[ext]`
    
- Downloading photos is slow so please optimize them and make use of concurrency

## Bonus Points

- Write tests
- Write your code in in a strongly typed language

## Submitting

- Create a  GitHub repo with clear readme instructions for running your code on MacOS or Linux
- Send us a zip of the files, or if it is easier for you, send us a link to the public repo containing your submission

Feel free to include “TODO:” comments if there are things that you might want to address but that would take too much time for this exercise We can talk about what the implementations might look like during the interview.

Please let us know if you have any questions!

---

Thanks,

HomeVision Engineering
