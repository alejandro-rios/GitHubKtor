<head>
    <link rel="stylesheet" href="https://fonts.googleapis.com/icon?family=Material+Icons">
    <link rel="stylesheet" href="https://code.getmdl.io/1.3.0/material.indigo-pink.min.css">
    <script defer src="https://code.getmdl.io/1.3.0/material.min.js"></script>
</head>
<body>
<div class="demo-layout mdl-layout mdl-js-layout mdl-layout--fixed-header">
    <header class="demo-header mdl-layout__header mdl-color--green-800 mdl-color-text--white">
        <div class="mdl-layout__header-row">
            <span class="mdl-layout-title ">GitHub Dashboard</span>
            <div class="mdl-layout-spacer"></div>
        </div>
    </header>
    <main class="mdl-layout__content mdl-color--grey-100">
        <div class="mdl-grid">
            <table class="mdl-data-table mdl-js-data-table mdl-color--white mdl-shadow--2dp mdl-cell mdl-cell--12-col">
                <thead>
                <tr>
                    <th class="mdl-data-table__cell--non-numeric">Name</th>
                    <th class="mdl-data-table__cell--non-numeric">Description</th>
                    <th class="mdl-data-table__cell--non-numeric">html</th>
                </tr>
                </thead>
                <tbody>
                <#list GitHubRepos as repo>
                    <tr>
                        <td class="mdl-data-table__cell--non-numeric">${repo.name}</td>
                        <td class="mdl-data-table__cell--non-numeric">${repo.description!?string}</td>
                        <td class="mdl-data-table__cell--non-numeric">${repo.htmlUrl}</td>
                    </tr> </#list> </tbody>
            </table>
        </div>
    </main>
</div>