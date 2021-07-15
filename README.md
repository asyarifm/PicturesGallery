# PicturesGallery

# PicturesGallery is an android application which developed using Kotlin.
# The application able to display pictures from unsplash API.
# As default 12 latest pictures (1 page) from unsplash are displayed in 3 x 3 Grid Layout.
# However, Once user reach the scroll end the app will load another 12 pictures, until it reach maximum page which is set to 5 pages

# images are displayed with username and description.
# username are in bold text meanwhile description in italic.
# not every image has description, the app will check if description value is empty, it will try get the value from alt_description.
# if both empty the description will not be displayed

# User able to switch the image layout between List layout and Grid layout by clicking the icon on the right top
# In this app user also able to search images, by clicking search icon and typing the query.

# This app utilize Volley library to send get request to fetch data from unsplash.
# Once receiving the response from unsplash, this app uses Gson library to do json parsing and put each json item to specific class parameter.

